package com.example.entrega02.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.entrega02.R
import com.example.entrega02.data.TouristicPlace
import com.example.entrega02.adapters.PropietaryScreenTouristicPlaceAdapter
import com.example.entrega02.data.Review
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class PropietarySearchActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: PropietaryScreenTouristicPlaceAdapter
    private lateinit var userEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prop_search_view)

        // Initialize views
        recyclerView = findViewById(R.id.prop_recyclerView)
        searchView = findViewById(R.id.prop_searchView)

        //read Email
        userEmail = intent.getStringExtra("email") ?: ""

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PropietaryScreenTouristicPlaceAdapter(mutableListOf(), userEmail)
        recyclerView.adapter = adapter

        loadTouristicPlacesFromFirebase()

        // Set up SearchView listener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView.clearFocus()
                adapter.filter.filter(query)
                return false
            }
            override fun onQueryTextChange(newText: String): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.prop_bottom_navigation)

        // Set listener for BottomNavigationView items (deprecated only means keep using this version XD)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, PropietaryScreen::class.java)
                    intent.putExtra("email", userEmail)
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.navigation_search -> {
                    true
                }

                R.id.navigation_profile -> {
                    val intent = Intent(this, PropietaryProfileActivity::class.java)
                    intent.putExtra("email", userEmail)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.addPlace ->{
                    val intent = Intent(this, AddPlace::class.java)
                    intent.putExtra("email", userEmail)
                    startActivity(intent)
                    finish()
                    true
                }

                else -> false
            }
        }
    }

    private fun loadTouristicPlacesFromFirebase() {
        val placesCollection = "places"
        val db = FirebaseFirestore.getInstance()
        val placesRef = db.collection(placesCollection)
            .whereEqualTo("email", userEmail)

        placesRef.get()
            .addOnSuccessListener { documents ->
                val touristicPlaces = mutableListOf<TouristicPlace>()
                for (document in documents) {
                    val name = document.getString("placeName") ?: ""
                    val picture = document.getString("url") ?: ""
                    val latitude = document.getString("latitude") ?: ""
                    val longitude = document.getString("longitude") ?: ""
                    val coordinates = arrayListOf(latitude, longitude)
                    val placeDescription = document.getString("placeDescription")?: ""
                    val ID = document.id

                    val reviewsRef = db.collection("placeReviews")
                        .whereEqualTo("placeName", name) // Query reviews by placeID
                    reviewsRef.get()
                        .addOnSuccessListener { reviewsDocuments ->
                            val scores = reviewsDocuments.mapNotNull { reviewDocument ->
                                reviewDocument.getDouble("score")?.toFloat()
                            }
                            val reviews = reviewsDocuments.mapNotNull { reviewDocument ->
                                reviewDocument.toObject(Review::class.java)
                            }
                            val touristicPlace = TouristicPlace(ID,name, picture, scores as ArrayList<Float>, coordinates, reviews as ArrayList<Review>,placeDescription)
                            touristicPlaces.add(touristicPlace)
                            adapter.updateData(touristicPlaces)
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this,getString(R.string.error_cargando_datos_lo_sentimos),
                                Toast.LENGTH_LONG).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this,getString(R.string.error_cargando_datos_lo_sentimos), Toast.LENGTH_LONG).show()
            }
    }
}
