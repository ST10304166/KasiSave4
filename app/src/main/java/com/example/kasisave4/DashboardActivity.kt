package com.example.kasisave4

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Bottom navigation buttons
        val homeButton = findViewById<ImageButton>(R.id.imageButton)
        val expensesButton = findViewById<ImageButton>(R.id.imageButton3)
        val analysisButton = findViewById<ImageButton>(R.id.imageButton2)

        homeButton.setOnClickListener {
            Toast.makeText(this, "You're already on the Home page.", Toast.LENGTH_SHORT).show()
        }

//        expensesButton.setOnClickListener {
//            val intent = Intent(this, ExpensesActivity::class.java)
//            startActivity(intent)
//        }

//        analysisButton.setOnClickListener {
//            val intent = Intent(this, AnalysisActivity::class.java)
//            startActivity(intent)
//        }

        // Settings icon click
        val settingsIcon = findViewById<ImageView>(R.id.settings_icon)
        settingsIcon.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }
    }
}
