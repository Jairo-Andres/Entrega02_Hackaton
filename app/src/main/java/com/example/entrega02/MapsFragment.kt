package com.example.entrega02

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONObject
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.Polyline

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MapsFragment : Fragment(), SensorEventListener {
    private lateinit var gMap: GoogleMap
    val sydney = LatLng(-34.0, 151.0)
    private lateinit var dogMarker: Marker
    var zoomLevel = 15.0f
    var moveCamera = true

    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor

    private var polylinePoints = mutableListOf<LatLng>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var currentLocation: LatLng = LatLng(0.0, 0.0) // Variable global para almacenar la ubicación actual del usuario
    private var currentPolyline: Polyline? = null
    private var currentMarker: Marker? = null

    private fun drawPolyline() {
        gMap.addPolyline(PolylineOptions().addAll(polylinePoints).color(Color.GREEN))
    }

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        gMap = googleMap
        gMap.uiSettings.isZoomControlsEnabled = false
        gMap.uiSettings.isCompassEnabled = true
        gMap.setMapStyle(
            context?.let { MapStyleOptions.loadRawResourceStyle(it, R.raw.map_day) })

        dogMarker = gMap.addMarker(
            MarkerOptions().position(sydney).title("A donde Vamos?!")
                .icon(context?.let { bitmapDescriptorFromVector(it, R.drawable.sujeto) })
        )!!
        gMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        gMap.setOnMapLongClickListener { latLng -> addPoint(latLng) }

        val callback = OnMapReadyCallback { googleMap ->
            // ...
            gMap.setOnMarkerClickListener { marker ->
                // Muestra la información del marcador
                marker.showInfoWindow()
                true
            }
            // Si el marcador que se tocó es el marcador del sujeto, no lo elimines

          /*  if (marker != dogMarker) {
                // Elimina la polilínea del mapa
                currentPolyline?.remove()

                // Elimina el marcador del mapa
               // marker.remove()

            }
           */
            true
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        sensorManager = context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())


        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        }
        // En la otra parte del código donde obtienes la ubicación del usuario
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Actualiza la ubicación actual del usuario
                currentLocation = LatLng(location?.latitude ?: 0.0, location?.longitude ?: 0.0)

                // Solo intenta agregar el punto si se pudo obtener la ubicación del usuario
                if (location != null) {
                    addPoint(currentLocation)
                }
            }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
            } else {
                // Permiso denegado
            }
        }
    }

    //From https://stackoverflow.com/questions/42365658/custom-marker-in-google-maps-in-android-with-vector-asset-icon
    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable?.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable!!.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        val smallMarker = Bitmap.createScaledBitmap(bitmap, 100, 100, false)
        return BitmapDescriptorFactory.fromBitmap(smallMarker)
    }

    fun moveDog(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        dogMarker.position = latLng
        dogMarker.zIndex = 10.0f
        polylinePoints.add(latLng)
        drawPolyline()
        if(moveCamera) {
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
        }
    }

    // Decodifica una polyline
    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(latLng)
        }

        return poly
    }

    private fun addPoint(latLng: LatLng) {

        // Elimina el marcador actual si existe
        currentMarker?.remove()
        currentPolyline?.remove()

        try {
            val geocoder = context?.let { Geocoder(it) }
            val addresses: MutableList<Address>? =
                geocoder?.getFromLocation(latLng.latitude, latLng.longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val addressText = address.getAddressLine(0)
                currentMarker = gMap.addMarker(
                    MarkerOptions().position(latLng).title(addressText).icon(
                        context?.let { bitmapDescriptorFromVector(it, R.drawable.puntom) })
                )
            } else {
                currentMarker = gMap.addMarker(
                    MarkerOptions().position(latLng).title("Ubicación desconocida").icon(
                        context?.let { bitmapDescriptorFromVector(it, R.drawable.puntom) })
                )
            }
        } catch (e: IOException) {
            // Maneja la excepción aquí, por ejemplo, mostrando un mensaje al usuario
        }

        // Crea una solicitud a la API de OSRM
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://router.project-osrm.org/route/v1/driving/${currentLocation.longitude},${currentLocation.latitude};${latLng.longitude},${latLng.latitude}?overview=full")
            .build()

        // Envía la solicitud en un hilo secundario
        CoroutineScope(Dispatchers.IO).launch {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                // Obtiene la respuesta
                val responseData = response.body?.string()

                // Convierte la respuesta a JSON
                val json = JSONObject(responseData)

                // Obtiene la ruta
                val routes = json.getJSONArray("routes")
                val route = routes.getJSONObject(0)
                val geometry = route.getString("geometry")

                // Convierte la geometría a una lista de LatLng
                val coordinates = decodePolyline(geometry)

                // Dibuja la ruta en el mapa en el hilo principal
                CoroutineScope(Dispatchers.Main).launch {
                    val polylineOptions = PolylineOptions()
                        .addAll(coordinates)
                        .color(Color.RED)
                    currentPolyline = gMap.addPolyline(polylineOptions)
                }
            }
        }
    }



    fun addStore(location: LatLng, title: String, desc: String) {
        val geocoder = context?.let { Geocoder(it) }
        val addresses: MutableList<Address>? =
            geocoder?.getFromLocation(location.latitude, location.longitude, 1)

        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            val addressText = address.getAddressLine(0)
            gMap.addMarker(
                MarkerOptions().position(location).title("$title, $addressText").snippet(desc).icon(
                    context?.let { bitmapDescriptorFromVector(it, R.drawable.puntobusqueda) })
            )
        } else {
            gMap.addMarker(
                MarkerOptions().position(location).title(title).snippet(desc).icon(
                    context?.let { bitmapDescriptorFromVector(it, R.drawable.puntobusqueda) })
            )
        }
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }


    override fun onSensorChanged(event: SensorEvent?) {
        if (this::gMap.isInitialized) {
            if (event!!.values[0] > 100) {
                gMap.setMapStyle(
                    context?.let { MapStyleOptions.loadRawResourceStyle(it, R.raw.map_day) })
            } else {
                gMap.setMapStyle(
                    context?.let { MapStyleOptions.loadRawResourceStyle(it, R.raw.map_night) })
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //Do nothing
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}