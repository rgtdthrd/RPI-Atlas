package com.example.myapplication

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.example.myapplication.databinding.ActivityMainBinding
import android.util.Log  // Add this import


class MainActivity : AppCompatActivity() {

    private lateinit var userLocationAccessor: UserLocationAccessor

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the UserLocationAccessor
        userLocationAccessor = UserLocationAccessor(this, this)

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
        val location = userLocationAccessor.getUserLocation()
        location?.let { (latitude, longitude) ->
            // Log the latitude and longitude to the console (Logcat)
            Log.d("Location", "Latitude: $latitude, Longitude: $longitude")
        } ?: run {
            // Log that the location is null (either permission not granted or no location available)
            Log.d("Location", "Location is null, permission might not be granted.")
        }
    }
}