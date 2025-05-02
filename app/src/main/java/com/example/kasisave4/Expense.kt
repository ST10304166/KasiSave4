package com.example.kasisave4

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val amount: Double,
    val date: String,
    val category: String,
    val receiptPath: String? = null,   // Optional file path
    val picturePath: String? = null    // Optional file path
)