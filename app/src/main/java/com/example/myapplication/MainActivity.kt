package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log // Add this import
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityMainBinding

var userCurrPosition = Pair(0.0, 0.0)
var landMarkGraph = Graph()

class MainActivity : AppCompatActivity() {
    private lateinit var userLocationAccessor: UserLocationAccessor
    private lateinit var userRotationAccessor: UserRotationAccessor
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var resultsAdapter: ResultsAdapter
    private lateinit var recyclerViewResults: RecyclerView
    private lateinit var cardView: CardView

    private var isClick = false

    // Handler for scheduling tasks
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateTask: Runnable // Declare the task
    private var userLoc: Pair<Float, Float> = Pair(0.0f, 0.0f)

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        accessibilityMode = sharedPreferences.getBoolean("accessibilityMode", false)
        val savedSpeedIndex = sharedPreferences.getInt("WalkingSpeed", 0)
        val speedMap = mapOf(0 to 0.075, 1 to 0.25, 2 to 0.333)
        currentSpeed = speedMap[savedSpeedIndex] ?: 0.075

        val campusMap: ImageView = findViewById(R.id.mapImage)
        val marker: ImageView = findViewById(R.id.markerImage)
        val edgeContainer: FrameLayout = findViewById(R.id.edgeContainer)
        marker.bringToFront()
        // initialize graph
        val landmarkList = landMarkGraph.parseLandmarksFromCSV(this, R.raw.landmarkdata)
        for (landmark in landmarkList) {
            landMarkGraph.addNode(landmark)
        }
        landMarkGraph.parseNodesFromCSV(this, R.raw.nodedata)
        landMarkGraph.parseEdgesFromCSV(this, R.raw.edgedata)
        // LandMarkGraph.ParseEdgesFromCSV(this, R.raw.edgedata)


        val endRouteButton = findViewById<Button>(R.id.endRouteButton)
        endRouteButton.setOnClickListener {
            landMarkGraph.endCurrentRoute(edgeContainer)
            endRouteButton.visibility = View.GONE
        }

        // hide endRouteButton
        endRouteButton.visibility = View.GONE

        // Initialize the UserLocationAccessor
        userLocationAccessor = UserLocationAccessor(this, this)

        userLocationAccessor.getUserLocation { coordinates ->
            if (coordinates != null) {
                userCurrPosition = Pair(coordinates.first, coordinates.second)
                userLoc = convertLocation(coordinates.first, coordinates.second)
            }
        }

        userRotationAccessor = UserRotationAccessor(this)

        // Display Landmark Names
        val displayLandmarkNames = findViewById<DisplayLandmarkNames>(R.id.landmarkView)
        displayLandmarkNames.init(campusMap, landMarkGraph)

        val allTerms = landMarkGraph.getAllLandmarkNodeNames()
        // val startNode = landMarkGraph.getNodeByName("Barton Hall")!!
        Log.d(
            "MainActivity",
            "User current location is ${userCurrPosition.first}, ${userCurrPosition.second}",
        )
        //val testLocation = landMarkGraph.getNodeByName("Folsom Library")
        //landMarkGraph.startRoute(testLocation!!, this, edgeContainer, campusMap)
        // var startNode = landMarkGraph.getClosestNode(userCurrPosition)!!
//        val endNode = landMarkGraph.getNodeByName("Folsom Library")!!
        // val route = landMarkGraph.shortestPath(startNode, endNode)
        // displaying route here using current location defaults
        // to (0,0) as start for some reason
        // landMarkGraph.startRoute(endNode, this, edgeContainer, campusMap)

        // Define the task to run every 3 seconds
        updateTask =
            object : Runnable {
                override fun run() {
                    // Request user location and update display
                    userLocationAccessor.getUserLocation { coordinates ->
                        if (coordinates != null) {
                            // Update test location and rotation
                            userCurrPosition = Pair(coordinates.first, coordinates.second)
                            userLoc = convertLocation(coordinates.first, coordinates.second)
                            Log.d("LocationTest", "User is at ${userLoc.first}, ${userLoc.second}")
                            displayLocation(campusMap, marker, userLoc.first, userLoc.second)
                            val testRot = convertRotation(userRotationAccessor.getUserRotation())
                            // Log.d("UpdateTask", "User is facing $testRot degrees from East")
                            displayRotation(campusMap, marker, testRot)
                        }
                    }

                    // Schedule the next run in 3 seconds (5000 milliseconds)
                    handler.postDelayed(this, 3000)
                }
            }

        // Setup search results list for displaying in the app
        cardView = findViewById(R.id.cardView)
        recyclerViewResults = findViewById(R.id.recyclerView)
        recyclerViewResults.layoutManager = LinearLayoutManager(this)
        resultsAdapter = ResultsAdapter(emptyList())
        recyclerViewResults.adapter = resultsAdapter

        // Updates the search result list as the user types in the search bar
        val searchView: SearchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                // When user first types in to search
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query != null) {
                        val results = fuzzySearch(query, allTerms)
                        displayResults(results)
                    }
                    return true
                }
                // When user changes edits search query
                override fun onQueryTextChange(newText: String?): Boolean {
                    // FuzzySearch
                    if (newText != null) {
                        val results = fuzzySearch(newText, allTerms)
                        displayResults(results)
                    }
                    return true
                }
            },
        )

        // Display setting page when user clicks on setting button
        findViewById<ImageButton>(R.id.SettingButton).setOnClickListener {
            displaySettingPage(this)
        }

        // Actions for mouse events
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
                    // Hide the SearchView and keyboard popup when the user clicks outside
                    if (isClick) {
                        v.performClick()
                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(searchView.windowToken, 0)
                        recyclerViewResults.visibility = View.GONE
                        cardView.visibility = View.GONE
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

    // Displays search results
    private fun displayResults(results: Array<String>) {
        if (results.isNotEmpty()) {
            // Update RecyclerView with new results
            resultsAdapter.updateData(results.toList())
            recyclerViewResults.visibility = View.VISIBLE
            cardView.visibility = View.VISIBLE
        } else {
            // Optionally, display a message in the UI rather than a Toast
            Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show()
            recyclerViewResults.visibility = View.GONE
            cardView.visibility = View.GONE
            resultsAdapter.updateData(emptyList())
        }
    }

    // Display the selected search result information
    fun onSearchResultSelected(selectedName: String) {
        // Find the SearchableNode corresponding to the selected name
        val selectedNode = landMarkGraph.getLandmarkNodeByName(selectedName)
        if (selectedNode != null) {
            // Update the marker position
            val location =
                convertLocation(selectedNode.position.first, selectedNode.position.second)
            displayLocation(
                findViewById(R.id.mapImage),
                findViewById(R.id.markerImage),
                location.first,
                location.second,
            )

            // zoom into the location
            // val zoomLayout = findViewById<ZoomLayout>(R.id.zoomLayout)
            // zoomLayout.zoomTo(2f, true)

            // take user to info & options page
            displayLocationInfo(this, selectedNode)
        } else {
            Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
        }

        // Hide the RecyclerView
        recyclerViewResults.visibility = View.GONE

        // Hide the keyboard and clear focus
        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.clearFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchView.windowToken, 0)
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
        return navController.navigateUp(appBarConfiguration) ||
            super.onSupportNavigateUp()
    }


    override fun onResume() {
        super.onResume()
        userLocationAccessor.stopLocationUpdates()
        handler.post(updateTask)

        // start route if requested when returning to main map screen
        val routeStarted = intent.getBooleanExtra("routeStarted", false)
        if (routeStarted){
            val nodeName = intent.getStringExtra("nodeName")
            val destination = landMarkGraph.getLandmarkNodeByName(nodeName!!)
            val destinationNode = destination?.let { landMarkGraph.getClosestNode(it.position) }
            val userLocationAccessor = UserLocationAccessor(this, this)
            userLocationAccessor.getUserLocation { coordinates ->
                if (coordinates != null) {
                    userCurrPosition = Pair(coordinates.first, coordinates.second)
                    userLoc = convertLocation(coordinates.first, coordinates.second)
                }
            }
            landMarkGraph.startRoute(destinationNode!!, this, findViewById(R.id.edgeContainer), findViewById(R.id.mapImage))
            findViewById<Button>(R.id.endRouteButton).visibility = View.VISIBLE
            intent.removeExtra("routeStarted")
        }
    }
}
