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

class SignUpActivity : AppCompatActivity() {

    private lateinit var firstNameInput: EditText
    private lateinit var surnameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var createAccountButton: Button
    private lateinit var signInText: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        firstNameInput = findViewById(R.id.firstNameInput)
        surnameInput = findViewById(R.id.surnameInput)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        createAccountButton = findViewById(R.id.createAccountButton)
        signInText = findViewById(R.id.signInText)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        createAccountButton.setOnClickListener {
            val firstName = firstNameInput.text.toString().trim()
            val surname = surnameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (firstName.isEmpty()) {
                firstNameInput.error = "First name is required"
                return@setOnClickListener
            }

            if (surname.isEmpty()) {
                surnameInput.error = "Surname is required"
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {
                emailInput.error = "Invalid Gmail (min 4 characters before @gmail.com)"
                return@setOnClickListener
            }

            if (!isValidPassword(password)) {
                passwordInput.error = "Password must include 1 uppercase letter & 1 number"
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                        val user = User(firstName, surname, email, password)

                        firestore.collection("users").document(uid).set(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Firestore Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        val message = task.exception?.localizedMessage ?: "Sign-up failed"
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                }
        }

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
