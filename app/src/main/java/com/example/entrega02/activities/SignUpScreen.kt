package com.example.entrega02.activities

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.entrega02.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SignUpScreen: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_screen)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        val signUpButton = findViewById<Button>(R.id.btnSignUp)
        val emailText = findViewById<EditText>(R.id.SignUpEmail)
        val passwordText = findViewById<EditText>(R.id.SignUpPassword)
        val checkPasswordText = findViewById<EditText>(R.id.checkPasswordText)
        val propietaryCheckBox = findViewById<CheckBox>(R.id.propietaryCheckbox)

        signUpButton.setOnClickListener {
            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            val checkPassword = checkPasswordText.text.toString()
            val isPropietary = propietaryCheckBox.isChecked

            if (password == checkPassword) {
                // Create the user in Firebase Authentication
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            user?.let {
                                // Add additional info to Firestore
                                val userMap = hashMapOf(
                                    "email" to email,
                                    "isPropietary" to isPropietary
                                )
                                db.collection("users").document(user.uid)
                                    .set(userMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this,
                                            "Cuenta Creada exitosamente, ya puede iniciar sesión :)",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            this,
                                            "Error al crear la cuenta: $e",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        } else {
                            Toast.makeText(
                                baseContext, "Error al crear la cuenta.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(
                    baseContext, "Error, los campos de las contraseñas no coinciden :(",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
