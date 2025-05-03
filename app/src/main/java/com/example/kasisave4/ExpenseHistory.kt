package com.example.kasisave4

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.*
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
    private lateinit var categoryTotalsView: TextView
    private lateinit var categorySpinner: Spinner

    private val db by lazy { AppDatabase.getInstance(this) }

    private var fromDateStr: String = ""
    private var toDateStr: String = ""
    private var selectedCategory: String? = null

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val isoFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_history)

        recyclerView = findViewById(R.id.recyclerExpenses)
        categoryTotalsView = findViewById(R.id.textCategoryTotals)
        categorySpinner = findViewById(R.id.spinnerCategoryFilter)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val btnFrom = findViewById<Button>(R.id.btnFrom)
        val btnTo = findViewById<Button>(R.id.btnTo)
        val btnFilter = findViewById<Button>(R.id.btnFilter)

        btnFrom.setOnClickListener {
            showDatePicker { date ->
                fromDateStr = date
                btnFrom.text = "From: $date"
            }
        }

        btnTo.setOnClickListener {
            showDatePicker { date ->
                toDateStr = date
                btnTo.text = "To: $date"
            }
        }

        btnFilter.setOnClickListener {
            if (fromDateStr.isNotEmpty() && toDateStr.isNotEmpty()) {
                selectedCategory = if (categorySpinner.selectedItemPosition == 0) null
                else categorySpinner.selectedItem.toString()

                filterExpenses(fromDateStr, toDateStr, selectedCategory)
                showCategoryTotals(fromDateStr, toDateStr, selectedCategory)
            } else {
                Toast.makeText(this, "Select both dates first", Toast.LENGTH_SHORT).show()
            }
        }

        loadCategoriesIntoSpinner()
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            val selectedDate = String.format("%02d/%02d/%04d", day, month + 1, year)
            onDateSelected(selectedDate)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun toIso(date: String): String {
        return try {
            val parsed = dateFormat.parse(date)
            isoFormat.format(parsed!!)
        } catch (e: Exception) {
            Log.e("DATE_PARSE_ERROR", "Failed to convert $date to ISO format", e)
            ""
        }
    }

    private fun loadCategoriesIntoSpinner() {
        lifecycleScope.launch {
            val categories = db.expenseDao().getAllCategories()
            val uniqueCategories = listOf("All Categories") + categories.distinct()
            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(this@ExpenseHistory, android.R.layout.simple_spinner_item, uniqueCategories)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = adapter
            }
        }
    }

    private fun filterExpenses(from: String, to: String, category: String?) {
        lifecycleScope.launch {
            val fromIso = toIso(from)
            val toIso = toIso(to)

            val filtered = if (category == null) {
                db.expenseDao().getExpensesBetween(fromIso, toIso)
            } else {
                db.expenseDao().getExpensesByCategoryBetween(fromIso, toIso, category)
            }

            withContext(Dispatchers.Main) {
                if (filtered.isEmpty()) {
                    Toast.makeText(this@ExpenseHistory, "No expenses found in range", Toast.LENGTH_SHORT).show()
                }
                recyclerView.adapter = ExpenseAdapter(filtered)
            }
        }
    }

    private fun showCategoryTotals(from: String, to: String, category: String?) {
        lifecycleScope.launch {
            val fromIso = toIso(from)
            val toIso = toIso(to)

            val categoryTotals = db.expenseDao().getCategoryTotalsBetween(fromIso, toIso)

            withContext(Dispatchers.Main) {
                if (categoryTotals.isEmpty()) {
                    categoryTotalsView.text = "No category totals in this range."
                } else {
                    val summaryText = categoryTotals
                        .filter { category == null || it.category == category }
                        .joinToString("\n") {
                            "${it.category}: R%.2f".format(it.total)
                        }
                    categoryTotalsView.text = summaryText
                }
            }
        }
    }
}
