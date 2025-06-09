package com.example.kasisave4

data class Expense(
    val userId: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val category: String = "",
    val receiptUrl: String? = null,    // Firebase Storage URL
    val pictureUrl: String? = null,    // Firebase Storage URL
    val timestamp: Long = 0L           // Used for sorting
)
