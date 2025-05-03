package com.example.kasisave4

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Analysis : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var tvCurrentGoal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        db = AppDatabase.getInstance(this)

        val etMinGoal = findViewById<EditText>(R.id.etMinGoal)
        val etMaxGoal = findViewById<EditText>(R.id.etMaxGoal)
        val btnSaveGoal = findViewById<Button>(R.id.btnSaveGoal)
        tvCurrentGoal = findViewById(R.id.tvCurrentGoal)

        val currentMonth = getCurrentMonth()
        loadGoal(currentMonth) // Load the goal when the screen opens

        btnSaveGoal.setOnClickListener {
            val min = etMinGoal.text.toString().toDoubleOrNull()
            val max = etMaxGoal.text.toString().toDoubleOrNull()

            if (min == null || max == null || min > max) {
                Toast.makeText(this, "Please enter valid goal amounts", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val goal = SpendingGoal(minGoal = min, maxGoal = max, month = currentMonth)

            lifecycleScope.launch(Dispatchers.IO) {
                db.spendingGoalDao().insertSpendingGoal(goal)
                runOnUiThread {
                    Toast.makeText(this@Analysis, "Goal saved for $currentMonth", Toast.LENGTH_SHORT).show()
                    loadGoal(currentMonth) // Reload to show updated goal
                }
            }
        }
    }

    private fun loadGoal(month: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val goal = db.spendingGoalDao().getGoalForMonth(month)
            runOnUiThread {
                if (goal != null) {
                    tvCurrentGoal.text = "Current Goal for $month:\nMin: R${goal.minGoal}, Max: R${goal.maxGoal}"
                } else {
                    tvCurrentGoal.text = "Current Goal for $month: Not Set"
                }
            }
        }
    }

    private fun getCurrentMonth(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date())
    }
}
