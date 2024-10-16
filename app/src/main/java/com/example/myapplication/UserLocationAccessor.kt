package com.example.myapplication

import android.app.Activity
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

//Can probably add the getUserRotation function in this file

class UserLocationAccessor(private val context: Context, private val activity: Activity) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Obtain and return the device’s latitude and longitude coordinates as a pair of double-precision floating-point numbers.
    // Return null if unsuccessful
    fun getUserLocation(): Pair<Double, Double>? {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // Check if the app has permission to access the device's fine location
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Get the last known location using Google's FusedLocationProviderClient
            var result: Pair<Double, Double>? = null
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    result = Pair(location.latitude, location.longitude)
                }
            }.addOnFailureListener {
                result = null
            }
            return result
        } else {
            // Request permission if it was not granted
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return null
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123 // Define your request code here
    }
}