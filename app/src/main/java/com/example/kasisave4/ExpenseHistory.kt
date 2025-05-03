package com.example.kasisave4

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ExpenseHistory : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val db by lazy { AppDatabase.getInstance(this) }

    private var fromDateStr: String = ""
    private var toDateStr: String = ""

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val isoFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_history)

        recyclerView = findViewById(R.id.recyclerExpenses)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Find the buttons from the XML layout
        val btnFrom = findViewById<Button>(R.id.btnFrom)
        val btnTo = findViewById<Button>(R.id.btnTo)
        val btnFilter = findViewById<Button>(R.id.btnFilter)

        btnFrom.setOnClickListener { showDatePicker { date -> fromDateStr = date; btnFrom.text = "From: $date" } }
        btnTo.setOnClickListener { showDatePicker { date -> toDateStr = date; btnTo.text = "To: $date" } }

        btnFilter.setOnClickListener {
            if (fromDateStr.isNotEmpty() && toDateStr.isNotEmpty()) {
                filterExpenses(fromDateStr, toDateStr)
            } else {
                Toast.makeText(this, "Select both dates first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            val selectedDate = String.format("%02d/%02d/%04d", day, month + 1, year)
            onDateSelected(selectedDate)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun filterExpenses(from: String, to: String) {
        lifecycleScope.launch {
            // Convert 'from' and 'to' to ISO format strings
            val fromIso = toIso(from) // e.g., "20230501"
            val toIso = toIso(to) // e.g., "20230530"

            // Call the DAO with the formatted date strings
            val filtered = db.expenseDao().getExpensesBetween(fromIso, toIso)

            withContext(Dispatchers.Main) {
                recyclerView.adapter = ExpenseAdapter(filtered)
            }
        }
    }

    private fun toIso(date: String): String {
        return try {
            val parsed = dateFormat.parse(date)
            isoFormat.format(parsed!!)
        } catch (e: Exception) {
            ""
        }
    }
}
