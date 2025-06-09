package com.example.kasisave4

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class GraphActivity : AppCompatActivity() {

    private lateinit var btnSelectDateRange: Button
    private lateinit var barChart: BarChart
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var startDate: Date? = null
    private var endDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        btnSelectDateRange = findViewById(R.id.btnSelectDateRange)
        barChart = findViewById(R.id.barChart)

        btnSelectDateRange.setOnClickListener {
            showDateRangePicker()
        }
    }

    private fun showDateRangePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            calendar.set(year, month, day)
            startDate = calendar.time

            DatePickerDialog(this, { _, yearEnd, monthEnd, dayEnd ->
                calendar.set(yearEnd, monthEnd, dayEnd)
                endDate = calendar.time
                fetchAndDisplayData()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun fetchAndDisplayData() {
        val uid = auth.currentUser?.uid ?: return
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val monthKey = sdf.format(startDate ?: Date()) // assumes goal is stored under month of startDate

        firestore.collection("expenses")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { result ->
                val categoryTotals = mutableMapOf<String, Double>()

                for (doc in result) {
                    val rawTimestamp = doc.get("timestamp")
                    val timestamp: Date? = when (rawTimestamp) {
                        is Timestamp -> rawTimestamp.toDate()
                        is String -> try {
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(rawTimestamp)
                        } catch (e: Exception) {
                            null
                        }
                        is Long -> Date(rawTimestamp)
                        else -> null
                    }

                    if (timestamp != null && startDate != null && endDate != null &&
                        !timestamp.before(startDate) && !timestamp.after(endDate)
                    ) {
                        val category = doc.getString("category") ?: "Other"
                        val amount = doc.getDouble("amount") ?: 0.0
                        categoryTotals[category] = categoryTotals.getOrDefault(category, 0.0) + amount
                    }
                }

                // Fix 1: Use the correct collection name "spending_goals"
                firestore.collection("spending_goals")
                    .whereEqualTo("userId", uid)
                    .whereEqualTo("month", monthKey)
                    .get()
                    .addOnSuccessListener { goalResult ->
                        var minGoal = 0.0
                        var maxGoal = 0.0
                        if (!goalResult.isEmpty) {
                            val goal = goalResult.documents[0].toObject(SpendingGoal::class.java)
                            if (goal != null) {
                                minGoal = goal.minGoal
                                maxGoal = goal.maxGoal
                            }
                        }
                        renderBarChart(categoryTotals, minGoal, maxGoal)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to fetch goal: ${it.message}", Toast.LENGTH_SHORT).show()
                        renderBarChart(categoryTotals, 0.0, 0.0) // still show the graph
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch expenses: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun renderBarChart(categoryTotals: Map<String, Double>, minGoal: Double, maxGoal: Double) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        var index = 0
        for ((category, total) in categoryTotals.entries) {
            entries.add(BarEntry(index.toFloat(), total.toFloat()))
            labels.add(category)
            index++
        }

        val dataSet = BarDataSet(entries, "Amount per Category")
        dataSet.color = resources.getColor(R.color.purple_500, theme)

        val barData = BarData(dataSet)
        barChart.data = barData
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.xAxis.granularity = 1f
        barChart.xAxis.setDrawLabels(true)

        // Fix 2: Ensure X-axis labels show below bars
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        barChart.axisLeft.removeAllLimitLines()

        if (minGoal > 0.0) {
            val minLine = LimitLine(minGoal.toFloat(), "Min Goal")
            minLine.lineColor = resources.getColor(R.color.teal_200, theme)
            barChart.axisLeft.addLimitLine(minLine)
        }

        if (maxGoal > 0.0) {
            val maxLine = LimitLine(maxGoal.toFloat(), "Max Goal")
            maxLine.lineColor = resources.getColor(R.color.red, theme)
            barChart.axisLeft.addLimitLine(maxLine)
        }

        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.invalidate()
    }
}
