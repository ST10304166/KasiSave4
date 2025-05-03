package com.example.kasisave4

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpText: TextView
    private lateinit var db: AppDatabase  // reference to Room database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.createAccountButton)
        signUpText = findViewById(R.id.signUpText)

        // Initialize Room database
        db = AppDatabase.getInstance(this)

        // Sign up text click
        signUpText.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // Login button click
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            // Local validation
            if (!isValidEmail(email)) {
                emailInput.error = "Invalid Gmail (min 4 characters before @gmail.com)"
                return@setOnClickListener
            }

            if (!isValidPassword(password)) {
                passwordInput.error = "Password must have 1 uppercase letter and 1 number"
                return@setOnClickListener
            }

            // Database check
            lifecycleScope.launch {
                val user = withContext(Dispatchers.IO) {
                    db.userDao().getUser(email, password)
                }

                if (user != null) {
                    // Login success
                    val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Login failed
                    Toast.makeText(this@LoginActivity, "Incorrect email or password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Regex("^[a-zA-Z0-9._%+-]{4,}@gmail\\.com$").matches(email)
    }

    private fun isValidPassword(password: String): Boolean {
        return Regex("^(?=.*[A-Z])(?=.*\\d).{6,}$").matches(password)
    }
}
