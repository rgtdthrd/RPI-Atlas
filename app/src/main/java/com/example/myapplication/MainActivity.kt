package com.example.myapplication

import android.annotation.SuppressLint

import android.view.MotionEvent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import com.example.myapplication.databinding.ActivityMainBinding
import java.lang.Math.random
import android.util.Log  // Add this import


class MainActivity : AppCompatActivity() {

    private lateinit var userLocationAccessor: UserLocationAccessor


    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var isClick = false
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Initialize the UserLocationAccessor
        userLocationAccessor = UserLocationAccessor(this, this)

        var testLoc = ConvertLocation(42.73236423119569, -73.67009163262247)

        // Test getUserLocation with other functions = not working for some reason

        userLocationAccessor.getUserLocation { coordinates ->
            if (coordinates != null) {
                testLoc = ConvertLocation(coordinates.first, coordinates.second)
            }
            Log.d("Location", "Latitude: ${testLoc.first}, Longitude: ${testLoc.second}")
        }


        var testRot = ConvertRotation(random() * 360)

        val campusMap: ImageView = findViewById(R.id.mapImage)
        val marker: ImageView = findViewById(R.id.markerImage)

        marker.bringToFront()
        campusMap.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isClick = true
                    Log.d("MainActivity", "Image touched")

                    DisplayLocation(campusMap, marker, testLoc.first, testLoc.second)
                    DisplayRotation(campusMap, marker, testRot)

                    //testLoc = ConvertLocation(42.72845472653638 + (random() * 0.01),
                    //    -73.68341858852392 +(random() * 0.01))
                    testRot = ConvertRotation(90.0)
                }
                MotionEvent.ACTION_MOVE -> {
                    isClick = false
                    Log.d("MainActivity", "Image moved")
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        v.performClick()
                    }
                    Log.d("MainActivity", "Touch released")
                }
                MotionEvent.ACTION_CANCEL -> {
                    Log.d("MainActivity", "Touch canceled (gesture interrupted)")
                }
            }
            true
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    //testing getUserLocation
    override fun onResume() {
        super.onResume()

        val userLocationAccessor = UserLocationAccessor(context = this, activity = this)

        // Get a single location update and use it
        userLocationAccessor.getUserLocation { coordinates ->
            if (coordinates != null) {
                Log.d("Location", "Latitude: ${coordinates.first}, Longitude: ${coordinates.second}")
            } else {
                Log.d("Location", "Location is null or permission not granted.")
            }
        }
    }
}

