package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private const val EARTHRADIUS = 6366707.0195
var currentSpeed = 0.075 // in km/min
private var seedNode = LandmarkNode(Pair(0.0, 0.0), "N/A")
// private var graph = Graph() // different instance of graph?

// Activity for displaying location info and options
class LocationInfoAndOptionsActivity : AppCompatActivity() {
    // get userLocationAccessor so that we can get their location
    private lateinit var userLocationAccessor: UserLocationAccessor

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.location_info_and_options)
        // fill out text fields with location info
        findViewById<TextView>(R.id.locationName).text = seedNode.name
        findViewById<TextView>(R.id.latitude_text).text = "Latitude: ${seedNode.position.first}"
        findViewById<TextView>(R.id.longitude_text).text = "Longitude: ${seedNode.position.second}"

        // start route button to begin route to selected landmark
        val startRouteButton = findViewById<Button>(R.id.startRouteButton)
        startRouteButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("routeStarted", true)
            intent.putExtra("nodeName", seedNode.name)
            startActivity(intent)
            finish()
        }

        val graph: Graph = landMarkGraph

        // set up back button
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // Close the activity
        }
        userLocationAccessor = UserLocationAccessor(this, this)

        // coordinates: user location
        // seedNode: chosen landmark
        // get the distance between the user and the landmark
        userLocationAccessor.getUserLocation { coordinates ->
            if (coordinates != null) {
                val nearestNode = graph.getClosestNode(coordinates)
                val endNode = graph.getClosestNode(seedNode.position)
                if (nearestNode != null) {
                    println("NearNode is ${nearestNode.name}")
                    println("seedNode is ${seedNode.name}")
                    val route = graph.shortestPath(nearestNode, endNode!!)
                    println("Route distance: ${route.calculateDistance()}")
                    val eta = route.calculateDistance() / currentSpeed
                    findViewById<TextView>(R.id.ETA_text).text =
                        "Estimated Arrival Time: ${ BigDecimal(eta).setScale(2, RoundingMode.HALF_UP).toDouble()} mins"
                }

                findViewById<TextView>(R.id.distance_text).text =
                    "Distance: ${calculateDistance(seedNode.position, coordinates)} km"
            }
        }
    }
}

// Calculate the distance between two points on the earth's surface
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

// Start the location info activity
fun displayLocationInfo(
    context: Context,
    locationNode: LandmarkNode,
) {
    seedNode = locationNode
    val intent = Intent(context, LocationInfoAndOptionsActivity::class.java)
    context.startActivity(intent)
}
