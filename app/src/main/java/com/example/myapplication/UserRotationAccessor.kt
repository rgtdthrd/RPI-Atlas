package com.example.myapplication
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class UserRotationAccessor(private val context: Context) {

    var sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    var accelerometerReading = FloatArray(3)
    var magnetometerReading = FloatArray(3)
    var rotationMatrix = FloatArray(9)
    var orientationAngles = FloatArray(3)

    // Sensor event listener
    private val rotationEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    accelerometerReading = event.values
                    //Log.d("UserRotation", "Accelerometer reading: ${accelerometerReading.joinToString()}")
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    magnetometerReading = event.values
                    //Log.d("UserRotation", "Magnetometer reading: ${magnetometerReading.joinToString()}")
                }
            }

            // Check if both readings are valid before calculating the rotation
            if (accelerometerReading.isNotEmpty() && magnetometerReading.isNotEmpty()) {
                getUserRotation()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            Log.d("UserRotation", "Sensor ${sensor.name} accuracy changed to $accuracy")
        }
    }

    init {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (accelerometer != null && magnetometer != null) {
            sensorManager.registerListener(rotationEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(rotationEventListener, magnetometer, SensorManager.SENSOR_DELAY_UI)
            Log.d("UserRotation", "Sensors registered successfully")
        } else {
            Log.e("UserRotation", "Sensors not available on this device")
        }
    }

    // Function to get the user's facing direction in degrees from East
    fun getUserRotation(): Double {
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        val azimuthInRadians = orientationAngles[0].toDouble()
        val azimuthInDegrees = Math.toDegrees(azimuthInRadians)
        //Since we want the direction relative to East (0 degrees),
        // we subtract 90 from azimuthInDegrees.
        // Adding 360 ensures the result is non-negative,
        // and using % 360 confines it to a 0-360 degree range.
        val directionFromEast = (azimuthInDegrees - 90 + 360) % 360

        //Log.d("UserRotation", "User is facing $directionFromEast degrees from East")
        return directionFromEast
    }

    // Unregister the sensor listener when no longer needed
    fun stopRotationUpdates() {
        sensorManager.unregisterListener(rotationEventListener)
        Log.d("UserRotation", "Sensor listener unregistered")
    }
}
