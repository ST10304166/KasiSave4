package com.example.kasisave4

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Analysis : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        db = AppDatabase.getInstance(this)

        val etMinGoal = findViewById<EditText>(R.id.etMinGoal)
        val etMaxGoal = findViewById<EditText>(R.id.etMaxGoal)
        val btnSaveGoal = findViewById<Button>(R.id.btnSaveGoal)

        btnSaveGoal.setOnClickListener {
            val min = etMinGoal.text.toString().toDoubleOrNull()
            val max = etMaxGoal.text.toString().toDoubleOrNull()
            val month = getCurrentMonth()

            if (min == null || max == null || min > max) {
                Toast.makeText(this, "Please enter valid goal amounts", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val goal = SpendingGoal(minGoal = min, maxGoal = max, month = month)

            lifecycleScope.launch {
                db.spendingGoalDao().insertSpendingGoal(goal)
                Toast.makeText(this@Analysis, "Goal saved for $month", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentMonth(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date())
    }
}
