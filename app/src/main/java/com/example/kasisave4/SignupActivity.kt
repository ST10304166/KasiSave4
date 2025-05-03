package com.example.kasisave4

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var createAccountButton: Button
    private lateinit var signInText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        createAccountButton = findViewById(R.id.createAccountButton)
        signInText = findViewById(R.id.signInText)

        val db = AppDatabase.getInstance(this)
        val userDao = db.userDao()

        createAccountButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            // Validate input
            if (!isValidEmail(email)) {
                emailInput.error = "Invalid Gmail (min 4 characters before @gmail.com)"
                return@setOnClickListener
            }

            if (!isValidPassword(password)) {
                passwordInput.error = "Password must include 1 uppercase letter & 1 number"
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                val existingUser = userDao.getUserByEmail(email)
                if (existingUser != null) {
                    withContext(Dispatchers.Main) {
                        emailInput.error = "Email already exists"
                        Toast.makeText(this@SignUpActivity, "This email is already registered", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val newUser = User(email = email, password = password)
                    userDao.insertUser(newUser)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@SignUpActivity, "Account created!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                        finish()
                    }
                }
            }
        }

        signInText.setOnClickListener {
            startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
            finish()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Regex("^[a-zA-Z0-9._%+-]{4,}@gmail\\.com$").matches(email)
    }

    private fun isValidPassword(password: String): Boolean {
        return Regex("^(?=.*[A-Z])(?=.*\\d).{6,}$").matches(password)
    }
}
