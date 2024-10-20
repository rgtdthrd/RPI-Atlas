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
import android.os.Handler
import android.os.Looper

class MainActivity : AppCompatActivity() {

    private lateinit var userLocationAccessor: UserLocationAccessor


    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var isClick = false

    // Handler for scheduling tasks
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateTask: Runnable  // Declare the task
    private var userLoc: Pair<Int, Int> = Pair(0, 0)
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Initialize the UserLocationAccessor
        userLocationAccessor = UserLocationAccessor(this, this)

        userLocationAccessor.getUserLocation { coordinates ->
            if (coordinates != null) {
                userLoc = ConvertLocation(coordinates.first, coordinates.second)
            }
        }
        val testRot = ConvertRotation(random() * 360)

        val campusMap: ImageView = findViewById(R.id.mapImage)
        val marker: ImageView = findViewById(R.id.markerImage)
        marker.bringToFront()

        // Define the task to run every 3 seconds
        updateTask = object : Runnable {
            override fun run() {
                // Request user location and update display


                userLocationAccessor.getUserLocation { coordinates ->
                    if (coordinates != null) {
                        // Update test location and rotation
                        userLoc = ConvertLocation(coordinates.first, coordinates.second)
                        DisplayLocation(campusMap, marker, userLoc.first, userLoc.second)
                        DisplayRotation(campusMap, marker, testRot)
                    }
                }
                // Schedule the next run in 3 seconds (5000 milliseconds)
                handler.postDelayed(this, 1000)
            }
        }
        campusMap.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isClick = true
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
        userLocationAccessor.stopLocationUpdates()
        handler.post(updateTask)
    }
}

