package com.example.kasisave4

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SpendingGoalWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateSpendingGoalsWidget(context)
    }

    companion object {
        fun updateSpendingGoalsWidget(context: Context) {
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_spending_goal)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, SpendingGoalWidgetProvider::class.java)

            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                remoteViews.setTextViewText(R.id.tv_min_goal, "Spent: Login required")
                remoteViews.setTextViewText(R.id.tv_max_goal, "")
                appWidgetManager.updateAppWidget(thisWidget, remoteViews)
                return
            }

            val userId = user.uid
            val firestore = FirebaseFirestore.getInstance()

            val expensesTask = firestore.collection("expenses")
                .whereEqualTo("userId", userId)
                .get()

            val goalsTask = firestore.collection("spending_goals")
                .whereEqualTo("userId", userId)
                .get()

            com.google.android.gms.tasks.Tasks.whenAllSuccess<Any>(expensesTask, goalsTask)
                .addOnSuccessListener { results ->
                    val expensesSnapshot = results[0] as com.google.firebase.firestore.QuerySnapshot
                    val goalsSnapshot = results[1] as com.google.firebase.firestore.QuerySnapshot

                    val totalSpent = expensesSnapshot.documents
                        .mapNotNull { it.getDouble("amount") }
                        .sum()

                    val maxGoal = goalsSnapshot.documents
                        .mapNotNull { it.getDouble("maxGoal") }
                        .maxOrNull() ?: 0.0

                    Log.d("Widget", "Total spent: $totalSpent")
                    Log.d("Widget", "Max goal: $maxGoal")

                    remoteViews.setTextViewText(R.id.tv_min_goal, "Spent: R%.2f".format(totalSpent))
                    remoteViews.setTextViewText(R.id.tv_max_goal, "Max Goal: R%.2f".format(maxGoal))

                    appWidgetManager.updateAppWidget(thisWidget, remoteViews)
                }
                .addOnFailureListener { e ->
                    Log.e("Widget", "Failed to fetch data", e)
                    remoteViews.setTextViewText(R.id.tv_min_goal, "Error loading data")
                    remoteViews.setTextViewText(R.id.tv_max_goal, "")
                    appWidgetManager.updateAppWidget(thisWidget, remoteViews)
                }
        }

    }
}
