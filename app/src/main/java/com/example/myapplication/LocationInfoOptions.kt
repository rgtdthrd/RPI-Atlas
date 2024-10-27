package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMainBinding

class LocationInfoAndOptionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.location_info_and_options)
    }
}

fun DisplayLocationInfo(context: Context, locationNode: SearchableNode) {
    println("Location: ${locationNode.name}")
    val intent = Intent(context, LocationInfoAndOptionsActivity::class.java)
    context.startActivity(intent)
}