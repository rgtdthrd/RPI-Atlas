package com.example.myapplication
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import android.util.Log


class UserLocationAccessor(private val context: Context, private val activity: Activity) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // Location request parameters
    private val locationRequest = LocationRequest.create().apply {
        interval = 5000 // 5 seconds
        fastestInterval = 2000 // 2 seconds
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    // Function to get one location update and return coordinates as Pair
    fun getUserLocation(callback: (Pair<Double, Double>?) -> Unit) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.locations.firstOrNull()
                if (location != null) {
                    val coordinates = Pair(location.latitude, location.longitude)
                    Log.d("Location", "Latitude: ${coordinates.first}, Longitude: ${coordinates.second}")
                    callback(coordinates)
                } else {
                    Log.d("Location", "Location is null or permission not granted.")
                    callback(null)
                }
                // Stop location updates after receiving the first result
                stopLocationUpdates()
            }
        }

        // Check for permissions
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            callback(null)
        }
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }
}