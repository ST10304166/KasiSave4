package com.example.kasisave4

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var createAccountButton: Button
    private lateinit var signInText: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        createAccountButton = findViewById(R.id.createAccountButton)
        signInText = findViewById(R.id.signInText)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Handle Sign-Up Button
        createAccountButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            //Validate Email & Password
            if (!isValidEmail(email)) {
                emailInput.error = "Invalid Gmail (min 4 characters before @gmail.com)"
                return@setOnClickListener
            }

            if (!isValidPassword(password)) {
                passwordInput.error = "Password must include 1 uppercase letter & 1 number"
                return@setOnClickListener
            }

            // Create user in Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        val message = task.exception?.localizedMessage ?: "Sign-up failed"
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                }
        }

        //  Already have account → go to login
        signInText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
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
