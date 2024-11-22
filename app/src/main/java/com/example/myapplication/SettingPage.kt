package com.example.myapplication


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity


class SettingPage : AppCompatActivity() {
    private val speedModeMap = mapOf(
        0.075 to "Walking",
        0.25 to "Biking",
        0.333 to "Scootering"
    )
    private val modeSpeedMap = speedModeMap.entries.associate { it.value to it.key }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)
        val speedSpinner = findViewById<Spinner>(R.id.walking_speed_spinner)
        val modes = listOf("Walking", "Biking", "Scootering")
        val initialMode = speedModeMap[currentSpeed] ?: "Walking"
        val initialPosition = modes.indexOf(initialMode)

        ArrayAdapter.createFromResource(
            this,
            R.array.walking_speed_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            speedSpinner.adapter = adapter
        }
        speedSpinner.setSelection(initialPosition)
        speedSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedMode = modes[position]

                // 使用反向映射找到对应速度值并更新全局变量
                val selectedSpeed = modeSpeedMap[selectedMode] ?: 0.083
                currentSpeed = selectedSpeed
                Log.d("SettingPage", "Selected speed: $currentSpeed")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                //DO Nothing
            }
        }
        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }
    }


}
fun displaySettingPage(
    context: Context
) {
    val intent = Intent(context, SettingPage::class.java)
    context.startActivity(intent)
}