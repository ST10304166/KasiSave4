package com.example.kasisave4

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateCategoryActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_category)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val categoryNameEditText = findViewById<EditText>(R.id.editCategoryName)
        val saveCategoryButton = findViewById<Button>(R.id.btnSaveCategory)

        saveCategoryButton.setOnClickListener {
            val name = categoryNameEditText.text.toString().trim()
            val userId = auth.currentUser?.uid

            if (name.isEmpty()) {
                categoryNameEditText.error = "Enter a category name"
                return@setOnClickListener
            }

            if (userId == null) {
                Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val category = hashMapOf("name" to name)
            firestore.collection("users")
                .document(userId)
                .collection("categories")
                .add(category)
                .addOnSuccessListener {
                    Toast.makeText(this, "Category saved", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
