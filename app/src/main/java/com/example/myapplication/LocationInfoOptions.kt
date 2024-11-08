package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import kotlin.math.*

private val EARTHRADIUS = 6366707.0195
private var seed_node = LandmarkNode(Pair(0.0, 0.0), "N/A")
private var graph = Graph() // different instance of graph?
class LocationInfoAndOptionsActivity : AppCompatActivity() {
    private lateinit var userLocationAccessor: UserLocationAccessor
    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.location_info_and_options)
        findViewById<TextView>(R.id.locationName).text = seed_node.name
        findViewById<TextView>(R.id.latitude_text).text = "Latitude: ${seed_node.position.first}"
        findViewById<TextView>(R.id.longitude_text).text = "Longitude: ${seed_node.position.second}"

        val startRouteButton = findViewById<Button>(R.id.startRouteButton)
        startRouteButton.setOnClickListener {
            graph.StartRoute(seed_node)
            finish()
        }

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // Close the activity
        }
        userLocationAccessor = UserLocationAccessor(this, this)

        userLocationAccessor.getUserLocation { coordinates ->
            if (coordinates != null) {
                findViewById<TextView>(R.id.distance_text).text = "Distance: ${CalculateDistance(seed_node.position, coordinates)} km"
            }
        }
    }

}

fun CalculateDistance(p1: Pair<Double, Double>, p2: Pair<Double, Double>): Double {
    val lat1 = Math.toRadians(p1.first)
    val lon1 = Math.toRadians(p1.second)
    val lat2 = Math.toRadians(p2.first)
    val lon2 = Math.toRadians(p2.second)
    val exact_distance = acos(sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(lon2 - lon1)) * EARTHRADIUS
    return exact_distance.roundToInt() / 1000.0
}

fun DisplayLocationInfo(context: Context, locationNode: LandmarkNode) {
    seed_node = locationNode
    val intent = Intent(context, LocationInfoAndOptionsActivity::class.java)
    context.startActivity(intent)

}