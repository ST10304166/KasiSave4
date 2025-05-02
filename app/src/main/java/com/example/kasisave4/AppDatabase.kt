package com.example.kasisave4.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kasisave4.Expense
import com.example.kasisave4.ExpenseDao
import com.example.kasisave4.model.User

@Database(entities = [User::class, Expense::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // No need for const val here
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kasisave4_database" // You don't need const for this
                )
                    .fallbackToDestructiveMigration()  // During development only
                    .build()
                    .also { INSTANCE = it }
            }
    }
}