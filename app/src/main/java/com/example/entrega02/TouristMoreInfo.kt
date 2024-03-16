package com.example.entrega02

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso

class TouristMoreInfo: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.place_info)

        val myIntent = intent
        val derivedObject = myIntent.getSerializableExtra("object") as TouristicPlace

        val myTextView = findViewById<TextView>(R.id.InfoPlaceName)
        val myImageView = findViewById<ImageView>(R.id.InfoPlaceImage)
        val viewReviews = findViewById<FloatingActionButton>(R.id.viewReviews)
        // Set the text for the TextView
        myTextView.text = derivedObject.name

        // Load the image into the ImageView using Picasso
        Picasso.get().load(derivedObject.picture).into(myImageView)
        viewReviews.setOnClickListener{
            val intent = Intent(this, Reviews::class.java)
            intent.putExtra("object", derivedObject)
            startActivity(intent)
        }
    }
}