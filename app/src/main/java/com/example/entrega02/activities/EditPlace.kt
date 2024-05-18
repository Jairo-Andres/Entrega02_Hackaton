package com.example.entrega02.activities

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.entrega02.R
import com.example.entrega02.data.TouristicPlace
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class EditPlace : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    private lateinit var place: TouristicPlace
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_place)
        place = intent.getSerializableExtra("object") as TouristicPlace
        val propEmail = intent.getStringExtra("email")
        db = FirebaseFirestore.getInstance()
        val placeID = place.ID
        val name = place.name
        val description = place.placeDescription

        val infoPlaceName: EditText = findViewById(R.id.InfoPlaceName)
        val infoPlaceDescription: EditText = findViewById(R.id.InfoPlaceDescription)
        val placePicture: ImageView = findViewById(R.id.InfoPlaceImage)
        Picasso.get().load(place.picture).into(placePicture)

        infoPlaceName.setText(name)
        infoPlaceDescription.setText(description)

        val cancelButton: FloatingActionButton = findViewById(R.id.cancel)
        val saveButton: FloatingActionButton = findViewById(R.id.save)


        cancelButton.setOnClickListener {
            val intent = Intent(this, PropietaryScreen::class.java)
            intent.putExtra("email", propEmail)
            startActivity(intent)
        }


        saveButton.setOnClickListener {
            val placesCollection= db.collection("places")
            val placeName = infoPlaceName.text.toString()
            val placeDescription = infoPlaceDescription.text.toString()

            if ((placeName.isNotEmpty() && placeDescription.isNotEmpty()) && (placeName != name || placeDescription != description)) {
                placesCollection.document(placeID).update(
                    mapOf(
                        "email" to propEmail,
                        "placeName" to placeName,
                        "placeDescription" to placeDescription,
                        "url" to place.picture
                    )
                )
                Toast.makeText(this,
                    getString(R.string.datos_del_lugar_actualizados), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, getString(R.string.llenar_2_campos), Toast.LENGTH_SHORT).show()
            }
            val intent = Intent(this, PropietaryScreen::class.java)
            intent.putExtra("email", propEmail)
            startActivity(intent)
        }
    }
}
