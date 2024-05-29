package com.example.entrega02.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.entrega02.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class UpdateUserScreen: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_user)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        val updateButton = findViewById<Button>(R.id.btnSignUp)
        val emailText = findViewById<EditText>(R.id.SignUpEmail)
        val firstNameText = findViewById<EditText>(R.id.SignUpFirstName)
        val lastNameText = findViewById<EditText>(R.id.SignUpLastName)
        val personalInfoText = findViewById<EditText>(R.id.SignUpPersonalInfo)

        val userEmail = intent.getStringExtra("email")
        emailText.setText(userEmail)

        // Load existing user data
        val userId = auth.currentUser?.uid
        userId?.let { id ->
            db.collection("users").document(id).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    firstNameText.setText(document.getString("firstName"))
                    lastNameText.setText(document.getString("lastName"))
                    personalInfoText.setText(document.getString("personalInfo"))
                }
            }
        }

        updateButton.setOnClickListener {
            val email = emailText.text.toString()
            val firstName = firstNameText.text.toString()
            val lastName = lastNameText.text.toString()
            val personalInfo = personalInfoText.text.toString()

            // Update the user in Firestore
            val user = auth.currentUser
            user?.let {
                val userMap = hashMapOf(
                    "email" to email,
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "personalInfo" to personalInfo
                )
                db.collection("users").document(user.uid)
                    .update(userMap as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Información actualizada exitosamente.",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error al actualizar la información: $e",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }
}
