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

var user_curr_position = Pair(0.0, 0.0)

class MainActivity : AppCompatActivity() {
    private lateinit var userLocationAccessor: UserLocationAccessor
    private lateinit var userRotationAccessor: UserRotationAccessor
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var resultsAdapter: ResultsAdapter
    private lateinit var recyclerViewResults: RecyclerView
    private lateinit var cardView: CardView

    private var isClick = false
    private var landMarkGraph = Graph()
//    private var SearchResults = emptyArray<String>()

    // Handler for scheduling tasks
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateTask: Runnable // Declare the task
    private var userLoc: Pair<Float, Float> = Pair(0.0f, 0.0f)

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialize graph
        val edgeView: EdgeView = findViewById(R.id.edgeView)
        edgeView.setMap(findViewById(R.id.mapImage))
        val landmarkList = landMarkGraph.parseLandmarksFromCSV(this, R.raw.landmarkdata)
        landMarkGraph.parseNodesFromCSV(this, R.raw.nodedata)
        landMarkGraph.parseEdgesFromCSV(this, R.raw.edgedata)

        landMarkGraph.parseNodesFromCSV(this, R.raw.nodedata)
        // LandMarkGraph.ParseEdgesFromCSV(this, R.raw.edgedata)
        for (landmark in landmarkList) {
            landMarkGraph.addNode(landmark)
        }
        val allEdges = landMarkGraph.getAllEdges()
        val allTerms = landMarkGraph.getAllLandmarkNodeNames()
        for (edge in allEdges) {
            val firstNode = convertLocation(edge.start.position.first, edge.start.position.second)
            val secondNode = convertLocation(edge.end.position.first, edge.end.position.second)
            Log.d(
                "EdgeTest",
                "Edge: ${firstNode.first}, ${firstNode.second} -> ${secondNode.first}, ${secondNode.second}",
            )
            edgeView.addEdge(edge)
        }
        // Initialize the UserLocationAccessor
        userLocationAccessor = UserLocationAccessor(this, this)

        userLocationAccessor.getUserLocation { coordinates ->
            if (coordinates != null) {
                user_curr_position = Pair(coordinates.first, coordinates.second)
                userLoc = convertLocation(coordinates.first, coordinates.second)
            }
        }

        userRotationAccessor = UserRotationAccessor(this)
        var testRot = convertRotation(userRotationAccessor.getUserRotation())

        val campusMap: ImageView = findViewById(R.id.mapImage)
        val marker: ImageView = findViewById(R.id.markerImage)
        marker.bringToFront()

        // Define the task to run every 3 seconds
        updateTask =
            object : Runnable {
                override fun run() {
                    // Request user location and update display
                    userLocationAccessor.getUserLocation { coordinates ->
                        if (coordinates != null) {
                            // Update test location and rotation
                            user_curr_position = Pair(coordinates.first, coordinates.second)
                            userLoc = convertLocation(coordinates.first, coordinates.second)
                            Log.d("LocationTest", "User is at ${userLoc.first}, ${userLoc.second}")
                            displayLocation(campusMap, marker, userLoc.first, userLoc.second)
                            testRot = convertRotation(userRotationAccessor.getUserRotation())
                            // Log.d("UpdateTask", "User is facing $testRot degrees from East")
                            displayRotation(campusMap, marker, testRot)
                        }
                    }
                    // Schedule the next run in 3 seconds (5000 milliseconds)
                    handler.postDelayed(this, 1000)
                }
            }

        cardView = findViewById(R.id.cardView)
        recyclerViewResults = findViewById(R.id.recyclerView)
        recyclerViewResults.layoutManager = LinearLayoutManager(this)
        resultsAdapter = ResultsAdapter(emptyList())
        recyclerViewResults.adapter = resultsAdapter

        val searchView: SearchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    // 当用户提交查询时，调用 FuzzySearch
                    if (query != null) {
                        val results = fuzzySearch(query, allTerms)
                        /*Log.d("MainActivity", "Query: $query")
                        for (term in results)
                        {
                            Log.d("MainActivity", "Term: $term")
                        }*/
                        displayResults(results)
                    }
                    return true
                }

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
            DisplayLocationInfo(this, selectedNode)
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

    // testing getUserLocation
    override fun onResume() {
        super.onResume()
        userLocationAccessor.stopLocationUpdates()
        handler.post(updateTask)
    }
}
