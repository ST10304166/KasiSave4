package com.example.kasisave4

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadGoalProgressFromFirebase()

        setupBottomNavigation()
        setupIconClicks()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    Toast.makeText(this, "You're already on the Home page.", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_expenses -> {
                    startActivity(Intent(this, Expenses::class.java))
                    true
                }
                R.id.nav_analysis -> {
                    startActivity(Intent(this, Analysis::class.java))
                    true
                }
                R.id.nav_income -> {
                    startActivity(Intent(this, Income::class.java))
                    true
                }
                R.id.nav_graph -> {
                    startActivity(Intent(this, GraphActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupIconClicks() {
        val settingsIcon = findViewById<ImageView>(R.id.settings_icon)
        settingsIcon.setOnClickListener {
            startActivity(Intent(this, Settings::class.java))
        }

    }

    private fun loadGoalProgressFromFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val calendarStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val calendarEnd = Calendar.getInstance().apply {
            time = calendarStart.time
            add(Calendar.MONTH, 1)
        }

        val startTimestamp = calendarStart.timeInMillis
        val endTimestamp = calendarEnd.timeInMillis

        // Log the timestamps for debugging
        Log.d("FIRESTORE_DEBUG", "Start timestamp: $startTimestamp")
        Log.d("FIRESTORE_DEBUG", "End timestamp: $endTimestamp")
        Toast.makeText(this, "Range: $startTimestamp to $endTimestamp", Toast.LENGTH_LONG).show()

        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val goalDocId = "${userId}_$currentMonth"

        db.collection("spending_goals").document(goalDocId).get()
            .addOnSuccessListener { goalDoc ->
                val maxGoal = goalDoc.getDouble("maxGoal") ?: 0.0

                db.collection("expenses")
                    .whereEqualTo("userId", userId)
                    .whereGreaterThanOrEqualTo("timestamp", startTimestamp)
                    .whereLessThan("timestamp", endTimestamp)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.isEmpty) {
                            Toast.makeText(this, "No expenses found for this month.", Toast.LENGTH_SHORT).show()
                        }

                        val totalSpent = snapshot.sumOf { it.getDouble("amount") ?: 0.0 }
                        updateGoalProgress(totalSpent, maxGoal)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("FIRESTORE_ERROR", "Failed to load expenses", exception)
                        Toast.makeText(this, "Failed to load expenses: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("FIRESTORE_ERROR", "Failed to load goal data", exception)
                Toast.makeText(this, "Failed to load goal data: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateGoalProgress(currentSpending: Double, monthlyGoal: Double) {
        val progressBar = findViewById<ProgressBar>(R.id.progressBarGoal)
        val tvGoalSummary = findViewById<TextView>(R.id.tvGoalSummary)
        val tvGoalStatus = findViewById<TextView>(R.id.tvGoalStatus)

        tvGoalSummary.text = "Monthly Spending Progress"

        if (monthlyGoal <= 0) {
            progressBar.progress = 0
            tvGoalStatus.text = "No goal set"
            return
        }

        val progressPercent = ((currentSpending / monthlyGoal) * 100).coerceAtMost(100.0).toInt()
        progressBar.progress = progressPercent

        val statusText = when {
            currentSpending < monthlyGoal ->
                "Spent R%.2f of R%.2f (%.0f%%)".format(currentSpending, monthlyGoal, (currentSpending / monthlyGoal) * 100)
            currentSpending == monthlyGoal ->
                "Goal met! Spent R%.2f".format(currentSpending)
            else ->
                "Goal exceeded by R%.2f! Spent R%.2f".format(currentSpending - monthlyGoal, currentSpending)
        }

        tvGoalStatus.text = statusText
    }
}
