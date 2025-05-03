package com.example.kasisave4

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    suspend fun getAllExpenses(): List<Expense>

    // ✅ NEW: Fetch expenses within a date range
    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getExpensesBetween(startDate: String, endDate: String): List<Expense>

    @Query("SELECT DISTINCT category FROM expenses")
    suspend fun getAllCategories(): List<String>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate AND category = :category ORDER BY date ASC")
    suspend fun getExpensesByCategoryBetween(startDate: String, endDate: String, category: String): List<Expense>

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE date BETWEEN :startDate AND :endDate GROUP BY category")
    suspend fun getCategoryTotalsBetween(startDate: String, endDate: String): List<CategoryTotal>

}

data class CategoryTotal(
    val category: String,
    val total: Double
)

