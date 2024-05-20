package com.example.entrega02.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.entrega02.R
import com.example.entrega02.adapters.TouristScreenTouristicPlaceAdapter
import com.example.entrega02.data.Review
import com.example.entrega02.data.TouristicPlace
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

private const val PERM_LOCATION_CODE = 103

class TouristScreen : AppCompatActivity() {

    private lateinit var userEmail : String
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TouristScreenTouristicPlaceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tourist_recycler_view)
        userEmail = intent.getStringExtra("email") ?: ""
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = TouristScreenTouristicPlaceAdapter(mutableListOf(), userEmail)
        recyclerView.adapter = adapter

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {

                    true
                }
                R.id.navigation_search -> {
                    finish()
                    startActivity(Intent(this, TouristSearchActivity::class.java))
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("email", userEmail)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.navigation_map -> {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        startMapActivity()
                    } else {
                        requestLocationPermission()
                    }
                    true
                }
                else -> false
            }
        }

        loadTouristicPlacesFromFirebase()
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Location permission is required to access this functionality 😭", Toast.LENGTH_LONG).show()
        }
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERM_LOCATION_CODE)
    }

    private fun startMapActivity() {
        startActivity(Intent(this, MapActivity::class.java))
        finish()
    }

    private fun loadTouristicPlacesFromFirebase() {
        val placesCollection = "places"
        val db = FirebaseFirestore.getInstance()
        val placesRef = db.collection(placesCollection)

        placesRef.get()
            .addOnSuccessListener { documents ->
                val touristicPlaces = mutableListOf<TouristicPlace>()
                for (document in documents) {
                    val name = document.getString("placeName") ?: ""
                    val picture = document.getString("url") ?: ""
                    val latitude = document.getString("latitude") ?: ""
                    val longitude = document.getString("longitude") ?: ""
                    val coordinates = arrayListOf(latitude, longitude)
                    val placeDescription = document.getString("placeDescription") ?: ""
                    val ID = document.id

                    val reviewsRef = db.collection("placeReviews")
                        .whereEqualTo("placeID", ID) // Query reviews by placeID
                    reviewsRef.get()
                        .addOnSuccessListener { reviewsDocuments ->
                            val scores = reviewsDocuments.mapNotNull { reviewDocument ->
                                reviewDocument.getDouble("score")?.toFloat()
                            }
                            val reviews = reviewsDocuments.mapNotNull { reviewDocument ->
                                reviewDocument.toObject(Review::class.java)
                            }
                            val touristicPlace = TouristicPlace(ID,name, picture, scores as ArrayList<Float>, coordinates, reviews as ArrayList<Review>, placeDescription)
                            touristicPlaces.add(touristicPlace)
                            adapter.updateData(touristicPlaces)
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this,getString(R.string.error_cargando_datos_lo_sentimos),Toast.LENGTH_LONG).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this,getString(R.string.error_cargando_datos_lo_sentimos),Toast.LENGTH_LONG).show()
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERM_LOCATION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startMapActivity()
            } else {
                Toast.makeText(this, "Location permission denied 😔", Toast.LENGTH_SHORT).show()
            }
        }
    }
}