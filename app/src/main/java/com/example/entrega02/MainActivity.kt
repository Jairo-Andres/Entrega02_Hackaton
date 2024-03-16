package com.example.entrega02

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.widget.Button
import com.example.entrega02.databinding.LoginOrRegisterBinding
import android.content.Intent
import android.widget.Toast


class MainActivity : ComponentActivity() {
    private lateinit var binding: LoginOrRegisterBinding
    private lateinit var btn_login: Button  // Declare logInButton
    private lateinit var btn_register: Button  // Declare logInButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginOrRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        btn_login = findViewById<Button>(R.id.btn_login)
        btn_register = findViewById<Button>(R.id.btn_register)
        //user chooses to login or register
        btn_login.setOnClickListener {
            startActivity(Intent(this, LoginScreen::class.java))
        }
        btn_register.setOnClickListener {
            startActivity(Intent(this, SignUpScreen::class.java))
        }
    }
}
