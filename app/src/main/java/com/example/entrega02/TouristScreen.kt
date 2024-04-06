package com.example.entrega02
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.Manifest
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

private const val FILE_NAME = "touristicPlaces.txt"
private const val PERM_LOCATION_CODE = 103

class TouristScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tourist_recycler_view)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val cardList = mutableListOf<TouristicPlace>()


        val places : ArrayList<TouristicPlace> = readTouristicPlacesFromTxtFile(this)
        for(place in places) {
            cardList.add(place)
        }

        val adapter = TouristScreenTouristicPlaceAdapter(cardList)
        recyclerView.adapter = adapter

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    finish()
                    startActivity(Intent(this, TouristScreen::class.java))
                    true
                }
                R.id.navigation_search -> {
                    finish()
                    startActivity(Intent(this, TouristSearchActivity::class.java))
                    true
                }
                R.id.navigation_profile -> {
                    finish()
                    startActivity(Intent(this, ProfileActivity::class.java))
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
    }


    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            Toast.makeText(this, "Location permission is required to access this functionality 😭", Toast.LENGTH_LONG).show()
        }
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERM_LOCATION_CODE)
    }
    private fun startMapActivity() {
        startActivity(Intent(this, MapActivity::class.java))
    }

    fun readTouristicPlacesFromTxtFile(context: Context): ArrayList<TouristicPlace> {
        val touristicPlaceList = ArrayList<TouristicPlace>()

        try {

            val inputStream: InputStream = context.assets.open(FILE_NAME)
            val reader = BufferedReader(InputStreamReader(inputStream))

            var line: String?


            while (reader.readLine().also { line = it } != null) {
                val parts = line?.split(";")

                if (parts?.size == 4) {
                    val name = parts[0]
                    val picture = parts[1]
                    val scoresString = parts[2].split(" ") // Splitting scores separated by space
                    val coordinates = parts[3].split(" ")
                    val scores = ArrayList<Float>()
                    val coordinateArray = ArrayList<String>()
                    // Convert each score to float and add to scores list
                    for (scoreString in scoresString) {
                        val score = scoreString.toFloatOrNull() ?: continue
                        scores.add(score)
                    }
                    for (coord in coordinates) {
                        coordinateArray.add(coord)
                    }
                    // Create an TouristicPlace object and add it to the list
                    val TouristicPlace = TouristicPlace(name, picture, scores, coordinateArray)
                    touristicPlaceList.add(TouristicPlace)
                }
            }


            inputStream.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }

        return touristicPlaceList
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
