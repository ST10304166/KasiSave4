package com.example.kasisave4

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

class Settings : AppCompatActivity() {

    private lateinit var darkModeSwitch: Switch
    private lateinit var notificationSwitch: Switch
    private lateinit var currencySpinner: Spinner
    private lateinit var backupButton: Button
    private lateinit var cardView: CardView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var textViews: List<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        // Load dark mode preference BEFORE setting content view
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("darkMode", false)

        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize views
        darkModeSwitch = findViewById(R.id.switchDarkMode)
        notificationSwitch = findViewById(R.id.switchNotifications)
        currencySpinner = findViewById(R.id.spinnerCurrency)
        backupButton = findViewById(R.id.buttonBackup)
        cardView = findViewById(R.id.settingsCard)

        // List of all text views that need color adjustment
        textViews = listOf(
            findViewById(R.id.textViewTitle),
            findViewById(R.id.textViewAppearance),
            findViewById(R.id.textViewAppearanceDesc),
            findViewById(R.id.textViewPreferences),
            findViewById(R.id.textViewCurrencyDesc),
            findViewById(R.id.textViewNotificationDesc),
            findViewById(R.id.textViewBackup),
            findViewById(R.id.textViewBackupDesc)
        )

        // Set initial switch state and apply theme colors
        darkModeSwitch.isChecked = isDarkMode
        applyThemeColors(isDarkMode)

        // Handle switch change
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("darkMode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            recreate() // Restart activity to apply theme
        }
    }

    private fun applyThemeColors(darkMode: Boolean) {
        if (darkMode) {
            cardView.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.black))
            textViews.forEach { it.setTextColor(ContextCompat.getColor(this, android.R.color.white)) }
        } else {
            cardView.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
            textViews.forEach { it.setTextColor(ContextCompat.getColor(this, android.R.color.black)) }
        }
    }
}
