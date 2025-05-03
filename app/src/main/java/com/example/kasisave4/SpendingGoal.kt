package com.example.kasisave4

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SpendingGoal")
data class SpendingGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val minGoal: Double,
    val maxGoal: Double,
    val month: String // Example: "2025-05"
)

