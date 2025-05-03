package com.example.kasisave4

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SpendingGoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpendingGoal(goal: SpendingGoal)

    @Query("SELECT * FROM SpendingGoal WHERE month = :month LIMIT 1")
    suspend fun getGoalForMonth(month: String): SpendingGoal?
}