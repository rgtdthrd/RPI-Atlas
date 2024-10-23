package com.example.myapplication

import android.annotation.SuppressLint

import android.widget.SearchView
import android.widget.Toast
import java.io.BufferedReader
import java.io.InputStreamReader
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
    private var LandMarkGraph = Graph()
    private var SearchResults = emptyArray<String>()

    // Handler for scheduling tasks
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateTask: Runnable  // Declare the task
    private var userLoc: Pair<Int, Int> = Pair(0, 0)
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //initialize graph
        val landmarkList = readCSVFromRaw()
        for (landmark in landmarkList) {
            LandMarkGraph.AddNode(landmark)
        }
        val all_terms = LandMarkGraph.GetAllSearchableNodeNames()

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

        val searchView: SearchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // 当用户提交查询时，调用 FuzzySearch
                if (query != null) {
                    val results = FuzzySearch(query, all_terms)
                    displayResults(results)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 当搜索框内容发生变化时，调用 FuzzySearch
                if (newText != null) {
                    val results = FuzzySearch(newText, all_terms)
                    displayResults(results)
                }
                return true
            }
        })

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
    private fun displayResults(results: Array<String>) {
        if (results.isNotEmpty()) {
            Toast.makeText(this, "Results: ${results.joinToString(", ")}", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun readCSVFromRaw(): List<SearchableNode> {
        val nodeList = mutableListOf<SearchableNode>()
        val inputStream = resources.openRawResource(R.raw.landmarkdata)
        val reader = BufferedReader(InputStreamReader(inputStream))

        try {
            var line: String?
            reader.readLine()  // skip the title line
            while (reader.readLine().also { line = it } != null) {
                line?.let {
                    val columns = it.split(",")
                    assert(columns.size == 3)
                    val name = columns[0]
                    val lat = columns[1]
                    val long = columns[2]
                    val tmp = SearchableNode(position = Pair(lat.toDouble(), long.toDouble()), name = name)
                    nodeList.add(tmp)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            reader.close()
        }

        return nodeList  // 返回不可变列表
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

