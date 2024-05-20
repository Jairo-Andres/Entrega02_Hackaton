package com.example.entrega02.activities
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.entrega02.R
import com.example.entrega02.adapters.ExperienciasAdapter
import com.example.entrega02.data.PermissionCodes
import com.example.entrega02.data.InfoUser
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Arrays

class ProfileActivity : AppCompatActivity() {

    private var isNotified = false
    private lateinit var fotoPaseador: ImageView
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private var photoURI: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            loadUserImageFromFirestore(userId)
            loadUserEmailFromFirestore(userId)
        }

        // Configuración de pickers de imagen
        setupImagePickers()

        // Aquí es donde debes poner el código
        val emailTextView = findViewById<TextView>(R.id.txt_email)
        val userEmail = intent.getStringExtra("email")
        emailTextView.text = userEmail

        fotoPaseador = findViewById(R.id.icn_perfil)

        setupImagePickers()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val avistamientos = Arrays.asList(
            InfoUser(R.drawable.img_plazabolivar, "Plaza de Bolivar", "Fue una visita mientras estaba de excursión con mi familia", "09/12/2023"),
            InfoUser(R.drawable.img_museobotero, "Museo Botero", "Todas las obras del Sr Botero me agradaron", "12/02/2024"),
            InfoUser(R.drawable.img_museonacional, "Museo Nacional", "Es interesante ver toda la Cultura Colombiana", "31/01/2024")
        )

        val adapter = ExperienciasAdapter(avistamientos)
        recyclerView.adapter = adapter

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        findViewById<CircleImageView>(R.id.icn_logout).setOnClickListener {
            val intent = Intent(applicationContext, LoginScreen::class.java)
            finish()
            startActivity(intent)
        }

        val notifyButton = findViewById<CircleImageView>(R.id.icn_notificacion)
        notifyButton.setOnClickListener {
            if (isNotified) {
                notifyButton.setImageResource(R.drawable.icn_notificacion_inactiva)
            } else {
                notifyButton.setImageResource(R.drawable.icn_notificacion)
            }
            isNotified = !isNotified
        }

        findViewById<ImageButton>(R.id.agregarFotoView).setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        findViewById<ImageButton>(R.id.tomarFotoView).setOnClickListener {
            handleCameraPermission()
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Set the map menu item as selected
        bottomNavigationView.selectedItemId = R.id.navigation_profile

        // Set listener for BottomNavigationView items
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, TouristScreen::class.java))
                    finish()
                    true
                }
                R.id.navigation_search -> {
                    startActivity(Intent(this, TouristSearchActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_map -> {
                    startActivity(Intent(this, MapActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

    }


    fun loadUserEmailFromFirestore(userId: String) {
        val ref = FirebaseFirestore.getInstance().collection("users").document(userId)
        ref.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userEmail = document.getString("email")
                    if (userEmail != null) {
                        val emailTextView = findViewById<TextView>(R.id.txt_email)
                        emailTextView.text = userEmail
                    }
                }
            }
            .addOnFailureListener {
                // Aquí puedes agregar un mensaje de registro o de depuración
            }
    }

    fun uploadImageToFirebaseStorage(imageUri: Uri, userId: String) {
        val ref = FirebaseStorage.getInstance().getReference("/images/$userId")
        ref.putFile(imageUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    saveUserImageToFirestore(uri.toString(), userId)
                }
            }
            .addOnFailureListener {
                // Manejo de error
            }
    }

    fun saveUserImageToFirestore(imageUrl: String, userId: String) {
        val ref = FirebaseFirestore.getInstance().collection("users").document(userId)
        ref.update("imageUrl", imageUrl)
            .addOnSuccessListener {
                println("Image URL saved successfully.")
            }
            .addOnFailureListener {
                // Manejo de error
            }
    }

    fun loadUserImageFromFirestore(userId: String) {
        val ref = FirebaseFirestore.getInstance().collection("users").document(userId)
        ref.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val imageUrl = document.getString("imageUrl")
                    if (imageUrl != null) {
                        Glide.with(this)
                            .load(imageUrl)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(fotoPaseador)
                    }
                }
            }
            .addOnFailureListener {
                // Manejo de error
            }
    }


    private fun setupImagePickers() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                fotoPaseador.setImageURI(uri)
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                userId?.let { id -> uploadImageToFirebaseStorage(uri, id) }
            }
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoURI?.let {
                    fotoPaseador.setImageURI(it)
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    userId?.let { id -> uploadImageToFirebaseStorage(it, id) }
                }
            }
        }
    }


    private fun handleCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.CAMERA
            ) -> {
                requestPermissions(
                    arrayOf(android.Manifest.permission.CAMERA),
                    PermissionCodes.CAMERA_PERMISSION_CODE
                )
            }
            else -> {
                requestPermissions(
                    arrayOf(android.Manifest.permission.CAMERA),
                    PermissionCodes.CAMERA_PERMISSION_CODE
                )
            }
        }
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Foto nueva")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Tomada desde la aplicacion del Taller 2")
        photoURI = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        photoURI?.let { uri ->
            takePictureLauncher.launch(uri)
        }
    }

    override fun onResume() {
        super.onResume()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            loadUserImageFromFirestore(userId)
        }
    }

}

