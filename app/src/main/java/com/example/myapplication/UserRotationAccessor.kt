package com.example.myapplication

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class UserRotationAccessor(
    private val context: Context,
) {
    private var rotationMatrix = FloatArray(9)
    private var orientationAngles = FloatArray(3)
    private var sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometerReading = FloatArray(3)
    private var magnetometerReading = FloatArray(3)
    private var smoothedAccelerometerReading = FloatArray(3)
    private var smoothedMagnetometerReading = FloatArray(3)

    // Smoothing factor for the low-pass filter (0 < alpha <= 1),
    // higher values (e.g., 0.2) for faster responsiveness or
    // lower values (e.g., 0.05) for more stability.
    private val alpha = 0.1f

    // Sensor event listener
    private val rotationEventListener =
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        accelerometerReading = event.values
                        smoothedAccelerometerReading = lowPassFilter(accelerometerReading, smoothedAccelerometerReading)
                    }

                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        magnetometerReading = event.values
                        smoothedMagnetometerReading = lowPassFilter(magnetometerReading, smoothedMagnetometerReading)
                    }
                }

                // Check if both readings are valid before calculating the rotation
                if (smoothedAccelerometerReading.isNotEmpty() && smoothedMagnetometerReading.isNotEmpty()) {
                    getUserRotation()
                }
            }

            override fun onAccuracyChanged(
                sensor: Sensor,
                accuracy: Int,
            ) {
                Log.d("UserRotation", "Sensor ${sensor.name} accuracy changed to $accuracy")
            }
        }

    init {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (accelerometer != null && magnetometer != null) {
            sensorManager.registerListener(
                rotationEventListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI,
            )
            sensorManager.registerListener(
                rotationEventListener,
                magnetometer,
                SensorManager.SENSOR_DELAY_UI,
            )
            Log.d("UserRotation", "Sensors registered successfully")
        } else {
            Log.e("UserRotation", "Sensors not available on this device")
        }
    }

    // Function to apply a low-pass filter to smooth sensor data
    private fun lowPassFilter(
        input: FloatArray,
        output: FloatArray,
    ): FloatArray {
        if (output.isEmpty()) {
            return input
        }
        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
        return output
    }

    // Function to get the user's facing direction in degrees from East
    fun getUserRotation(): Double {
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            smoothedAccelerometerReading,
            smoothedMagnetometerReading,
        )
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        val azimuthInRadians = orientationAngles[0].toDouble()
        val azimuthInDegrees = Math.toDegrees(azimuthInRadians)
        // Since we want the direction relative to East (0 degrees),
        // we subtract 90 from azimuthInDegrees.
        // Adding 360 ensures the result is non-negative,
        // and using % 360 confines it to a 0-360 degree range.
        val directionFromEast = (azimuthInDegrees - 90 + 360) % 360

        return directionFromEast
    }

    // Unregister the sensor listener when no longer needed
    fun stopRotationUpdates() {
        sensorManager.unregisterListener(rotationEventListener)
        Log.d("UserRotation", "Sensor listener unregistered")
    }
}
