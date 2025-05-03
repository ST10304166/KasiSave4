package com.example.kasisave4

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import kotlin.jvm.java

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

        // BottomNavigationView setup
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        // Optional: Set Home selected by default
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    Toast.makeText(this, "You're already on the Home page.", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_expenses -> {
                    startActivity(Intent(this, Expenses::class.java))
                    true
                }
                R.id.nav_analysis -> {
                    startActivity(Intent(this, Analysis::class.java))
                    true
                }
                R.id.nav_income -> {
                    startActivity(Intent(this, Income::class.java))
                    true
                }
                else -> false // Handles any unexpected menu item
            }
        }

        val milestoneCard = findViewById<MaterialCardView>(R.id.card_milestones)
        milestoneCard.setOnClickListener {
            val intent = Intent(this, Badges_Achievements::class.java)
            startActivity(intent)
        }

        // Settings icon click
        val settingsIcon = findViewById<ImageView>(R.id.settings_icon)
        settingsIcon.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
            }
      }
}
