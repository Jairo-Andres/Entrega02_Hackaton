package com.example.entrega02.activities
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.entrega02.R

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
