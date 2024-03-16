package com.example.entrega02
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)
        var goBackButton = findViewById<Button>(R.id.goBackButton)
        goBackButton.setOnClickListener {
            //say bye to this activity
            finish()
        }

    }
}
