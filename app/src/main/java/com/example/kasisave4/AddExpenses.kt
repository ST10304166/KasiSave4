package com.example.kasisave4

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddExpenses : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var selectedReceiptUri: String? = null
    private var capturedImageUri: String? = null
    private lateinit var photoUri: Uri
    private val REQUEST_CAMERA_PERMISSION = 1001

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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = AppDatabase.getInstance(this)

        val descEt = findViewById<EditText>(R.id.Cdescription)
        val amtEt = findViewById<EditText>(R.id.editAmount)
        val dateEt = findViewById<EditText>(R.id.editDate)
        dateEt.isFocusable = false
        dateEt.isClickable = true

        dateEt.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedMonth = String.format("%02d", selectedMonth + 1)
                val formattedDay = String.format("%02d", selectedDay)
                val formattedDate = "$selectedYear-$formattedMonth-$formattedDay"
                dateEt.setText(formattedDate)
            }, year, month, day)

            datePicker.show()
        }

        val spinner = findViewById<Spinner>(R.id.spinnerCategory)
        val submitBt = findViewById<Button>(R.id.btnSubmitExpense)
        val uploadBt = findViewById<Button>(R.id.btnUploadFile)
        val cameraBt = findViewById<ImageButton>(R.id.btnTakePicture)

        // Spinner setup
        val categories = listOf("— choose —", "Food", "Transport", "Utilities", "Other")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // File picker
        uploadBt.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }

        // Camera
        cameraBt.setOnClickListener {
            checkCameraPermissionAndLaunch()
        }

        // Submit
        submitBt.setOnClickListener {
            val desc = descEt.text.toString().trim()
            val amt = amtEt.text.toString().toDoubleOrNull()
            val dateInput = dateEt.text.toString().trim()

            if (desc.isEmpty()) {
                descEt.error = "Required"
                return@setOnClickListener
            }

            if (amt == null) {
                amtEt.error = "Invalid amount"
                return@setOnClickListener
            }

            if (dateInput.isEmpty()) {
                dateEt.error = "Required"
                return@setOnClickListener
            }

            // Convert date to ISO format
            val userFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val isoFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val parsedDate = userFormat.parse(dateInput)
            val date = isoFormat.format(parsedDate!!)

            val category = if (spinner.selectedItemPosition > 0)
                spinner.selectedItem as String else ""

            val expense = Expense(
                description = desc,
                amount = amt,
                date = date,
                category = category,
                receiptPath = selectedReceiptUri,
                picturePath = capturedImageUri
            )

            lifecycleScope.launch {
                db.expenseDao().insertExpense(expense)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddExpenses, "Expense Saved!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun launchCamera() {
        val photoFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "photo_${System.currentTimeMillis()}.jpg")
        photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        cameraLauncher.launch(photoUri)
    }

    private fun checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        } else {
            launchCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                Toast.makeText(this, "Camera permission is required to take pictures.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
