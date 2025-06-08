package com.example.kasisave4

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class Analysis : AppCompatActivity() {

    private lateinit var tvCurrentGoal: TextView
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        val etMinGoal = findViewById<EditText>(R.id.etMinGoal)
        val etMaxGoal = findViewById<EditText>(R.id.etMaxGoal)
        val btnSaveGoal = findViewById<Button>(R.id.btnSaveGoal)
        tvCurrentGoal = findViewById(R.id.tvCurrentGoal)

        val currentMonth = getCurrentMonth()
        val currentUserId = auth.currentUser?.uid

        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        loadGoal(currentMonth, currentUserId)

        btnSaveGoal.setOnClickListener {
            val min = etMinGoal.text.toString().toDoubleOrNull()
            val max = etMaxGoal.text.toString().toDoubleOrNull()

            if (min == null || max == null || min > max) {
                Toast.makeText(this, "Please enter valid goal amounts", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val goal = SpendingGoal(
                minGoal = min,
                maxGoal = max,
                month = currentMonth,
                userId = currentUserId
            )

            val docId = "${currentUserId}_$currentMonth"

            firestore.collection("spending_goals")
                .document(docId)
                .set(goal)
                .addOnSuccessListener {
                    Toast.makeText(this, "Goal saved for $currentMonth", Toast.LENGTH_SHORT).show()
                    loadGoal(currentMonth, currentUserId)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save goal", Toast.LENGTH_SHORT).show()
                }
        }
        // BottomNavigationView setup
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        // Optional: Set Home selected by default
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))

                    true
                }
                R.id.nav_expenses -> {
                    startActivity(Intent(this, Expenses::class.java))
                    true
                }
                R.id.nav_analysis -> {
                    Toast.makeText(this, "You're already on the Analysis Page", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_income -> {
                    startActivity(Intent(this, Income::class.java))
                    true
                }
                else -> false // Handles any unexpected menu item
            }
        }
    }


    private fun loadGoal(month: String, userId: String) {
        val docId = "${userId}_$month"

        firestore.collection("spending_goals")
            .document(docId)
            .get()
            .addOnSuccessListener { document ->
                val goal = document.toObject(SpendingGoal::class.java)
                if (goal != null) {
                    tvCurrentGoal.text = "Current Goal for $month:\nMin: R${goal.minGoal}, Max: R${goal.maxGoal}"
                } else {
                    tvCurrentGoal.text = "Current Goal for $month: Not Set"
                }
            }
            .addOnFailureListener {
                tvCurrentGoal.text = "Failed to load goal for $month"
            }
    }

    private fun getCurrentMonth(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date())
    }
}
