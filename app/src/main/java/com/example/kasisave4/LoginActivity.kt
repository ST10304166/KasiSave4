package com.example.kasisave4

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpText: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // UI
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton) // <-- Make sure your XML uses this ID
        signUpText = findViewById(R.id.signUpText)

        // Redirect to SignUp
        signUpText.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        // Handle Login
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (!isValidEmail(email)) {
                emailInput.error = "Invalid Gmail (min 4 characters before @gmail.com)"
                return@setOnClickListener
            }

            if (!isValidPassword(password)) {
                passwordInput.error = "Password must have 1 uppercase letter and 1 number"
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                        // Fetch user profile from Firestore
                        firestore.collection("users").document(userId).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val firstName = document.getString("firstName") ?: "User"
                                    Toast.makeText(this, "Welcome back, $firstName!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, DashboardActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this, "User profile not found in database.", Toast.LENGTH_LONG).show()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to load profile: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        val message = task.exception?.localizedMessage ?: "Login failed"
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
