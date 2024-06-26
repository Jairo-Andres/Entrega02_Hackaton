package com.example.entrega02.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.entrega02.MapsFragment
import com.example.entrega02.R
import com.example.entrega02.data.TouristicPlace
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
//import com.example.entrega02.MapActivityBinding
import com.example.entrega02.databinding.MapActivityBinding
import com.example.entrega02.utils.Alerts
import com.example.entrega02.utils.GeocoderSearch
import com.google.android.material.bottomnavigation.BottomNavigationView

class MapActivity : AppCompatActivity() {
    private lateinit var binding: MapActivityBinding
    var TAG = MainActivity::class.java.name

    private var alerts: Alerts = Alerts(this)
    private lateinit var geocoderSearch: GeocoderSearch
    private val PERM_LOCATION_CODE = 303
    private lateinit var position: Location
    private lateinit var fragment: MapsFragment
    private lateinit var targetCoordinates: LatLng

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: com.google.android.gms.location.LocationRequest
    private lateinit var locationCallback: LocationCallback
    var DISTANCE_RADIUS_KM = 10.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = MapActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val targetPlace = intent.getStringExtra("name")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupLocation()

        binding.zoomSlider.addOnChangeListener { _, value, _ ->
            DISTANCE_RADIUS_KM = value.toDouble()
            geocoderSearch.DISTANCE_RADIUS_KM = DISTANCE_RADIUS_KM
        }

        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                startLocationUpdates()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                alerts.indefiniteSnackbar(
                    binding.root,
                    "El permiso de Localizacion es necesario para usar esta actividad 😭"
                )
            }

            else -> {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERM_LOCATION_CODE
                )
            }
        }

        fragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as MapsFragment
        geocoderSearch = GeocoderSearch(this)
        if(targetPlace != null) {
            binding.searchField.editText?.setText(targetPlace)
        }
        binding.searchField.editText?.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    val text = binding.searchField.editText?.text.toString()
                    val address: MutableList<Address> = geocoderSearch.finPlacesByNameInRadius(
                        text,
                        LatLng(position.latitude, position.longitude)
                    )!!
                    address.forEach() {
                        var title = text
                        var desc =
                            if (it.getAddressLine(0).isNullOrEmpty()) it.getAddressLine(0) else ""
                        fragment.addPoint(LatLng(it.latitude, it.longitude))
                    }
                    true
                }

                else -> false
            }
        }
        binding.zoomSlider.addOnChangeListener { _, value, _ ->
            fragment.zoomLevel = value
        }
        binding.switchFollowDog.setOnCheckedChangeListener { _, isChecked ->
            fragment.moveCamera = isChecked
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Set the map menu item as selected
        bottomNavigationView.selectedItemId = R.id.navigation_map

        // Set listener for BottomNavigationView items
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    finish()
                    startActivity(Intent(this, TouristScreen::class.java))
                    finish()
                    true
                }
                R.id.navigation_search -> {
                    finish()
                    startActivity(Intent(this, TouristSearchActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_profile -> {
                    finish()
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_map -> {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    } else {
                    }
                    true
                }
                else -> false
            }
        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERM_LOCATION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates()
                } else {
                    alerts.shortSimpleSnackbar(
                        binding.root,
                        "Me acaban de negar los permisos de Localizacion 😭"
                    )
                }
            }
        }
    }

    private fun setupLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = com.google.android.gms.location.LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500).apply {
            setMinUpdateDistanceMeters(5F)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach { location ->
                    Log.i(TAG, "onLocationResult: $location")

                    // Llama a moveDog con la ubicación actual y un bearing predeterminado (0f)
                    fragment.moveDog(location, 0f)
                    position = location
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}