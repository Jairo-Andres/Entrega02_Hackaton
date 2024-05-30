package com.example.entrega02.activities
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.entrega02.R
import com.example.entrega02.adapters.SearchAdapter
import com.example.entrega02.adapters.TouristScreenTouristicPlaceAdapter
import com.example.entrega02.data.InfoUser
import com.example.entrega02.data.Review
import com.example.entrega02.data.TouristicPlace
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class TouristSearchActivity : AppCompatActivity() {

    private lateinit var searchAdapter1: SearchAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: TouristScreenTouristicPlaceAdapter
    private val PERM_LOCATION_CODE = 103
    private lateinit var userEmail:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_view)
        userEmail = intent.getStringExtra("email") ?: ""
        // Initialize views
        recyclerView = findViewById(R.id.recyclerView)
        searchView = findViewById(R.id.searchView)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TouristScreenTouristicPlaceAdapter(mutableListOf(), userEmail)
        recyclerView.adapter = adapter

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

        // Definir la lista de datos (Avistamiento) para el primer RecyclerView
        val avistamientos1 = listOf(
            InfoUser(R.drawable.img_malacoptila, "Malacoptila", "Familia de los Bucconidae", "08/01/2024"),
            InfoUser(R.drawable.img_guacharaca, "Guacharaca", "Ave de bosque", "02/03/2024"),
            InfoUser(R.drawable.img_loro_multicolor, "Loro multicolor", "Ave insignia de Caldas", "25/06/2024"),
        )

        // Instanciar y configurar el adaptador y RecyclerView para el primer conjunto de datos
        val recyclerView1: RecyclerView = findViewById(R.id.recyclerView1)
        searchAdapter1 = SearchAdapter(avistamientos1) { avis1 ->
            abrirDetalles(avis1)
        }
        recyclerView1.adapter = searchAdapter1
        recyclerView1.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Set the map menu item as selected
        bottomNavigationView.selectedItemId = R.id.navigation_search

        // Set listener for BottomNavigationView items
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, TouristScreen::class.java)
                    intent.putExtra("email", userEmail)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.navigation_search -> {
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
                            Toast.makeText(this,getString(R.string.error_cargando_datos_lo_sentimos),Toast.LENGTH_LONG).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this,getString(R.string.error_cargando_datos_lo_sentimos),Toast.LENGTH_LONG).show()
            }
    }

    private fun abrirDetalles(infoUser: InfoUser) {
        val intent = Intent(this, DetallesBusquedaActivity::class.java)
        // Aquí puedes agregar datos adicionales al intent si es necesario
        intent.putExtra("tipoAve", infoUser.tipoAve)
        intent.putExtra("descripcion", infoUser.descripcion)
        startActivity(intent)
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
}
