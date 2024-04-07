package com.example.entrega02

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.entrega02.databinding.LoginScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginScreen : AppCompatActivity() {
    private lateinit var binding: LoginScreenBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding = LoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var logInButton = findViewById<Button>(R.id.btnLogin)
        var emailText = findViewById<EditText>(R.id.editTextEmail)
        var passwordText = findViewById<EditText>(R.id.editTextPassword)

        logInButton.setOnClickListener {
            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // User logged in successfully
                            val currentUser = firebaseAuth.currentUser
                            currentUser?.let { user ->
                                // Check if user is proprietary
                                checkProprietaryStatus(user.uid, email)
                            }
                        } else {
                            Toast.makeText(
                                this, getString(R.string.credencialesInvalidas),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(
                    this, getString(R.string.noCamposBlanco),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun checkProprietaryStatus(userId: String, userEmail: String) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val isPropietary = document.getBoolean("isPropietary") ?: false
                    if (isPropietary) {
                        // User is proprietary, navigate to proprietary screen
                        val intent = Intent(this, PropietaryScreen::class.java)
                        intent.putExtra("email",userEmail)
                        startActivity(intent)
                        finish()
                    } else {
                        // User is not proprietary, navigate to tourist screen
                        val intent = Intent(this, TouristScreen::class.java)
                        intent.putExtra("email",userEmail)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    // Document doesn't exist, handle accordingly
                    Toast.makeText(
                        this, "User data not found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { exception ->
                // Error occurred while fetching user data
                Toast.makeText(
                    this, "Error fetching user data: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
