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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddExpenses : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private var selectedReceiptUri: Uri? = null
    private var capturedImageUri: Uri? = null
    private lateinit var photoUri: Uri
    private val REQUEST_CAMERA_PERMISSION = 1001

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedReceiptUri = uri  // Store the real Uri, not a string
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            capturedImageUri = photoUri  // Store real Uri
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

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        val descEt = findViewById<EditText>(R.id.Cdescription)
        val amtEt = findViewById<EditText>(R.id.editAmount)
        val dateEt = findViewById<EditText>(R.id.editDate)
        val spinner = findViewById<Spinner>(R.id.spinnerCategory)
        val submitBt = findViewById<Button>(R.id.btnSubmitExpense)
        val uploadBt = findViewById<Button>(R.id.btnUploadFile)
        val cameraBt = findViewById<ImageButton>(R.id.btnTakePicture)

        dateEt.isFocusable = false
        dateEt.isClickable = true

        dateEt.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "%04d-%02d-%02d".format(selectedYear, selectedMonth + 1, selectedDay)
                dateEt.setText(formattedDate)
            }, year, month, day)

            datePicker.show()
        }

        val categories = listOf("— choose —", "Food", "Transport", "Utilities", "Other")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        uploadBt.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }

        cameraBt.setOnClickListener {
            checkCameraPermissionAndLaunch()
        }

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

            val userFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val isoFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val parsedDate = userFormat.parse(dateInput)
            val date = isoFormat.format(parsedDate!!)

            val category = if (spinner.selectedItemPosition > 0)
                spinner.selectedItem as String else ""

            if (category.isEmpty()) {
                Toast.makeText(this, "Please select a valid category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(this, "You must be logged in to add expenses", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = currentUser.uid
            val storageRef = storage.reference

            val receiptRef = selectedReceiptUri?.let {
                storageRef.child("receipts/$userId/${UUID.randomUUID()}")
            }

            val pictureRef = capturedImageUri?.let {
                storageRef.child("photos/$userId/${UUID.randomUUID()}")
            }

            val uploadTasks = mutableListOf<com.google.android.gms.tasks.Task<Uri>>()

            if (receiptRef != null && selectedReceiptUri != null) {
                val task = receiptRef.putFile(selectedReceiptUri!!).continueWithTask { upload ->
                    if (!upload.isSuccessful) throw upload.exception!!
                    receiptRef.downloadUrl
                }
                uploadTasks.add(task)
            }

            if (pictureRef != null && capturedImageUri != null) {
                val task = pictureRef.putFile(capturedImageUri!!).continueWithTask { upload ->
                    if (!upload.isSuccessful) throw upload.exception!!
                    pictureRef.downloadUrl
                }
                uploadTasks.add(task)
            }

            if (uploadTasks.isEmpty()) {
                saveExpenseToFirestore(userId, desc, amt, date, category, null, null)
            } else {
                com.google.android.gms.tasks.Tasks.whenAllSuccess<Uri>(uploadTasks)
                    .addOnSuccessListener { urls ->
                        val receiptUrl = if (selectedReceiptUri != null) urls.getOrNull(0)?.toString() else null
                        val pictureUrl = if (capturedImageUri != null) {
                            if (selectedReceiptUri != null && urls.size > 1) urls[1].toString()
                            else urls[0].toString()
                        } else null

                        saveExpenseToFirestore(userId, desc, amt, date, category, receiptUrl, pictureUrl)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Upload failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun saveExpenseToFirestore(
        userId: String,
        desc: String,
        amt: Double,
        date: String,
        category: String,
        receiptUrl: String?,
        pictureUrl: String?
    ) {
        val expenseData = hashMapOf(
            "userId" to userId,
            "description" to desc,
            "amount" to amt,
            "date" to date,
            "category" to category,
            "receiptUrl" to receiptUrl,
            "pictureUrl" to pictureUrl,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("expenses")
            .add(expenseData)
            .addOnSuccessListener {
                Toast.makeText(this, "Expense saved to Firestore!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving expense: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun launchCamera() {
        val photoFile = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "photo_${System.currentTimeMillis()}.jpg"
        )
        photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        cameraLauncher.launch(photoUri)
    }

    private fun checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
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
