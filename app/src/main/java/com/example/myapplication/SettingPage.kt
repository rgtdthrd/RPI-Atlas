package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

var accessibilityMode = false

class SettingPage : AppCompatActivity() {
    private val speedModeMap =
        mapOf(
            0.075 to "Walking",
            0.25 to "Biking",
            0.333 to "Scootering",
        )
    private lateinit var sharedPreferences: SharedPreferences
    private val modeSpeedMap = speedModeMap.entries.associate { it.value to it.key }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val speedSpinner = findViewById<Spinner>(R.id.walking_speed_spinner)
        val modes = listOf("Walking", "Biking", "Scootering")
        val initialMode = speedModeMap[currentSpeed] ?: "Walking"
        val initialPosition = modes.indexOf(initialMode)
        val accessibilitySwitch = findViewById<Switch>(R.id.Accessibility_switch)
        val savedAccessibilityMode = sharedPreferences.getBoolean("accessibilityMode", false)
        accessibilitySwitch.isChecked = savedAccessibilityMode

        accessibilitySwitch.setOnCheckedChangeListener { _, isChecked ->
            accessibilityMode = isChecked
            saveSetting("accessibilityMode", isChecked)
        }

        ArrayAdapter
            .createFromResource(
                this,
                R.array.walking_speed_options,
                android.R.layout.simple_spinner_item,
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                speedSpinner.adapter = adapter
            }
        speedSpinner.setSelection(initialPosition)
        speedSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    saveSetting("WalkingSpeed", position)
                    val selectedMode = modes[position]
                    val selectedSpeed = modeSpeedMap[selectedMode] ?: 0.083
                    currentSpeed = selectedSpeed
                    Log.d("SettingPage", "Selected speed: $currentSpeed")

                    // Automatically enable Accessibility Mode for Biking or Scootering
                    if (selectedMode == "Biking" || selectedMode == "Scootering") {
                        if (!accessibilitySwitch.isChecked) {
                            accessibilitySwitch.isChecked = true
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    // DO Nothing
                }
            }
        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun saveSetting(
        key: String,
        value: Any,
    ) {
        val editor = sharedPreferences.edit()
        when (value) {
            is Boolean -> editor.putBoolean(key, value)
            is Int -> editor.putInt(key, value)
        }
        editor.apply()
    }
}

fun displaySettingPage(context: Context) {
    val intent = Intent(context, SettingPage::class.java)
    context.startActivity(intent)
}
