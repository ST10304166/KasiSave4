package com.example.kasisave4

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.kasisave4.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AddExpenses : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var selectedReceiptUri: String? = null
    private var capturedImageUri: String? = null
    private lateinit var photoUri: Uri

    // Launchers
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedReceiptUri = uri?.toString()
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            capturedImageUri = photoUri.toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expenses)

        // Prevent content overlap with system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = AppDatabase.getInstance(this)

        // Get UI references
        val descEt = findViewById<EditText>(R.id.Cdescription)
        val amtEt = findViewById<EditText>(R.id.editAmount)
        val dateEt = findViewById<EditText>(R.id.editDate)
        val spinner = findViewById<Spinner>(R.id.spinnerCategory)
        val submitBt = findViewById<Button>(R.id.btnSubmitExpense)
        val uploadBt = findViewById<Button>(R.id.btnUploadFile)
        val cameraBt = findViewById<ImageButton>(R.id.btnTakePicture)

        // Set up spinner
        val categories = listOf("— choose —", "Food", "Transport", "Utilities", "Other")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // File picker button
        uploadBt.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }

        // Camera button
        cameraBt.setOnClickListener {
            val photoFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "photo_${System.currentTimeMillis()}.jpg")
            photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
            cameraLauncher.launch(photoUri)
        }

        // Submit button
        submitBt.setOnClickListener {
            val desc = descEt.text.toString().trim()
            val amt = amtEt.text.toString().toDoubleOrNull()
            val date = dateEt.text.toString().trim()

            if (desc.isEmpty()) {
                descEt.error = "Required"
                return@setOnClickListener
            }

            if (amt == null) {
                amtEt.error = "Invalid amount"
                return@setOnClickListener
            }

            if (date.isEmpty()) {
                dateEt.error = "Required"
                return@setOnClickListener
            }

            val category = if (spinner.selectedItemPosition > 0)
                spinner.selectedItem as String else ""

            val expense = Expense(
                description = desc,
                amount = amt,
                date = date,
                category = category,
                receiptPath = selectedReceiptUri,
                picturePath = capturedImageUri // ✅ Corrected
            )

            lifecycleScope.launch {
                db.expenseDao().insertExpense(expense)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddExpenses, "Saved!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
