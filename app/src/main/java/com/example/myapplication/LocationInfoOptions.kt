package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton

private var seed_node = SearchableNode(Pair(0.0, 0.0), "N/A")

class LocationInfoAndOptionsActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.location_info_and_options)
        findViewById<TextView>(R.id.locationName).text = seed_node.name
        findViewById<TextView>(R.id.latitude_text).text = "Latitude ${seed_node.position.first}"
        findViewById<TextView>(R.id.longitude_text).text = "Longitude ${seed_node.position.second}"

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // Close the activity
        }
    }

}

fun DisplayLocationInfo(context: Context, locationNode: SearchableNode) {
    seed_node = locationNode
    val intent = Intent(context, LocationInfoAndOptionsActivity::class.java)
    context.startActivity(intent)

}