package com.example.kasisave4

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewExpenses : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val db by lazy { AppDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_expenses)

        recyclerView = findViewById(R.id.recyclerExpenses)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load expenses from DB
        lifecycleScope.launch {
            val expenses = db.expenseDao().getAllExpenses()
            withContext(Dispatchers.Main) {
                recyclerView.adapter = ExpenseAdapter(expenses)
            }
        }
    }
}
