package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

private const val EARTHRADIUS = 6366707.0195
private const val HUMANSPEED  = 0.075 // in km/min
private var seedNode = LandmarkNode(Pair(0.0, 0.0), "N/A")
//private var graph = Graph() // different instance of graph?

class LocationInfoAndOptionsActivity : AppCompatActivity() {
    private lateinit var userLocationAccessor: UserLocationAccessor

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.location_info_and_options)
        findViewById<TextView>(R.id.locationName).text = seedNode.name
        findViewById<TextView>(R.id.latitude_text).text = "Latitude: ${seedNode.position.first}"
        findViewById<TextView>(R.id.longitude_text).text = "Longitude: ${seedNode.position.second}"

        val startRouteButton = findViewById<Button>(R.id.startRouteButton)
        startRouteButton.setOnClickListener {
            // commented out for now due to change in startRoute implementation.
            // graph.startRoute(seedNode)
            finish()
        }

        val graph: Graph = landMarkGraph

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // Close the activity
        }
        userLocationAccessor = UserLocationAccessor(this, this)

        // coordinates: user location
        // seedNode: chosen landmark
        userLocationAccessor.getUserLocation { coordinates ->
            if (coordinates != null) {
                val nearestNode = graph.getClosestNode(coordinates)
                if (nearestNode != null) {
                    println("NearNode is ${nearestNode.name}")
                    println("seedNode is ${seedNode.name}")
                    val route = graph.shortestPath(nearestNode, seedNode)
                    println("Route distance: ${route.calculateDistance()}")
                }

                findViewById<TextView>(R.id.distance_text).text =
                    "Distance: ${calculateDistance(seedNode.position, coordinates)} km"
            }
        }
    }
}

fun calculateDistance(
    p1: Pair<Double, Double>,
    p2: Pair<Double, Double>,
): Double {
    val lat1 = Math.toRadians(p1.first)
    val lon1 = Math.toRadians(p1.second)
    val lat2 = Math.toRadians(p2.first)
    val lon2 = Math.toRadians(p2.second)
    val exactDistance =
        acos(sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(lon2 - lon1)) * EARTHRADIUS
    return exactDistance.roundToInt() / 1000.0
}

fun displayLocationInfo(
    context: Context,
    locationNode: LandmarkNode,
) {
    seedNode = locationNode
    val intent = Intent(context, LocationInfoAndOptionsActivity::class.java)
    context.startActivity(intent)
}
