package com.example.kasisave4

data class SpendingGoal(
    val minGoal: Double = 0.0,
    val maxGoal: Double = 0.0,
    val month: String = "", // Format: "2025-05"
    val userId: String = "" // Optional: add if you're tracking user-specific goals
)