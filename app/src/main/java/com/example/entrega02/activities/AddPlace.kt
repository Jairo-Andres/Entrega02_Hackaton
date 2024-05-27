package com.example.entrega02.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.entrega02.R
import com.example.entrega02.activities.PropietaryScreen
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AddPlace : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var propEmail: String
    private lateinit var placePicture: ImageView
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var infoPlaceName: EditText
    private lateinit var infoPlaceDescription: EditText
    private var imageUri: Uri? = null
    private lateinit var bottomBar: BottomNavigationView

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val imageUri: Uri? = data?.data
            placePicture.setImageURI(imageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_place)
        propEmail = intent.getStringExtra("email") ?: ""
        db = FirebaseFirestore.getInstance()

        infoPlaceName = findViewById(R.id.InfoPlaceName)
        infoPlaceDescription = findViewById(R.id.InfoPlaceDescription)
        placePicture = findViewById(R.id.InfoPlaceImage)
        bottomBar = findViewById(R.id.bottom_navigation)

        val cancelButton: FloatingActionButton = findViewById(R.id.cancel)
        val saveButton: FloatingActionButton = findViewById(R.id.save)

        setupImagePickers()

        placePicture.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        bottomBar.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, PropietaryScreen::class.java)
                    intent.putExtra("email", propEmail)
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.navigation_search -> {
                    val intent = Intent(this, PropietarySearchActivity::class.java)
                    intent.putExtra("email", propEmail)
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.navigation_profile -> {
                    val intent = Intent(this, PropietaryProfileActivity::class.java)
                    intent.putExtra("email", propEmail)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.addPlace ->{
                    true
                }

                else -> false
            }
        }

        cancelButton.setOnClickListener {
            val intent = Intent(this, PropietaryScreen::class.java)
            intent.putExtra("email", propEmail)
            startActivity(intent)
        }

        saveButton.setOnClickListener {
            val placesCollection = db.collection("places")
            val placeName = infoPlaceName.text.toString()
            val placeDescription = infoPlaceDescription.text.toString()

            if (placeName.isNotEmpty() && placeDescription.isNotEmpty()) {

                if (imageUri != null) {
                    val storageRef = FirebaseStorage.getInstance().reference.child("placesPictures/${UUID.randomUUID()}")

                    storageRef.putFile(imageUri!!)
                        .addOnSuccessListener { uploadTask ->
                            uploadTask.storage.downloadUrl
                                .addOnSuccessListener { downloadUri ->
                                    val placeData = hashMapOf(
                                        "email" to propEmail,
                                        "placeName" to placeName,
                                        "placeDescription" to placeDescription,
                                        "url" to downloadUri.toString()
                                    )

                                    placesCollection.add(placeData)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Place added successfully", Toast.LENGTH_SHORT).show()
                                            val intent = Intent(this, PropietaryScreen::class.java)
                                            intent.putExtra("email", propEmail)
                                            startActivity(intent)
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this, "Error adding place: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupImagePickers() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = uri
                Glide.with(this@AddPlace)
                    .load(uri)
                    .into(placePicture)
            }
        }
    }
}
