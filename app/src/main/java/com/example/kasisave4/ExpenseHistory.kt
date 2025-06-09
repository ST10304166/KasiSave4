package com.example.kasisave4

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class ExpenseHistory : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryTotalsView: TextView
    private lateinit var categorySpinner: Spinner

    private val firestore = FirebaseFirestore.getInstance()

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
        firestore.collection("categories").get()
            .addOnSuccessListener { snapshot ->
                val categoryList = snapshot.documents.mapNotNull { it.getString("name") }
                val uniqueCategories = listOf("All Categories") + categoryList.distinct()
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, uniqueCategories)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load categories", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterExpenses(from: String, to: String, category: String?) {
        val fromIso = toIso(from)
        val toIso = toIso(to)

        var query: Query = firestore.collection("expenses")
            .whereGreaterThanOrEqualTo("date", fromIso)
            .whereLessThanOrEqualTo("date", toIso)

        if (category != null) {
            query = query.whereEqualTo("category", category)
        }

        query.get()
            .addOnSuccessListener { snapshot ->
                val expenses = snapshot.documents.mapNotNull {
                    val amount = it.getDouble("amount") ?: return@mapNotNull null
                    val date = it.getString("date") ?: ""
                    val cat = it.getString("category") ?: "Uncategorized"
                    Expense(date = date, amount = amount, category = cat)
                }

                if (expenses.isEmpty()) {
                    Toast.makeText(this, "No expenses found in range", Toast.LENGTH_SHORT).show()
                }

                recyclerView.adapter = ExpenseAdapter(expenses)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load expenses", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showCategoryTotals(from: String, to: String, category: String?) {
        val fromIso = toIso(from)
        val toIso = toIso(to)

        firestore.collection("expenses")
            .whereGreaterThanOrEqualTo("date", fromIso)
            .whereLessThanOrEqualTo("date", toIso)
            .get()
            .addOnSuccessListener { snapshot ->
                val totals = mutableMapOf<String, Double>()

                snapshot.documents.forEach { doc ->
                    val cat = doc.getString("category") ?: "Uncategorized"
                    val amount = doc.getDouble("amount") ?: 0.0

                    if (category == null || category == cat) {
                        totals[cat] = totals.getOrDefault(cat, 0.0) + amount
                    }
                }

                if (totals.isEmpty()) {
                    categoryTotalsView.text = "No category totals in this range."
                } else {
                    val summaryText = totals.entries.joinToString("\n") {
                        "${it.key}: R%.2f".format(it.value)
                    }
                    categoryTotalsView.text = summaryText
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load category totals", Toast.LENGTH_SHORT).show()
            }
    }
}
