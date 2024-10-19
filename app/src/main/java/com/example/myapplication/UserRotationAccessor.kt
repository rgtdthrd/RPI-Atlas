import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class OrientationHelper(context: Context) {

    private var sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometerReading = FloatArray(3)
    private var magnetometerReading = FloatArray(3)
    private var rotationMatrix = FloatArray(9)
    private var orientationAngles = FloatArray(3)

    // Declare the sensor event listener here as a val
    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> accelerometerReading = event.values
                Sensor.TYPE_MAGNETIC_FIELD -> magnetometerReading = event.values
            }

            // Update orientation if both accelerometer and magnetometer readings are available
            if (accelerometerReading.isNotEmpty() && magnetometerReading.isNotEmpty()) {
                GetUserRotation()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // No implementation needed for this example
        }
    }

    init {
        // Register sensors when the helper class is initialized
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    // Function to get the user's facing direction in degrees from East
    fun GetUserRotation(): Float {
        // Get the rotation matrix using accelerometer and magnetometer data
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)

        // Get the orientation angles (azimuth, pitch, roll)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        // Azimuth is the angle of rotation around the Z-axis (North = 0 degrees)
        val azimuthInRadians = orientationAngles[0]
        val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()

        // Adjust the azimuth to be relative to East (0 degrees = East, 90 = North, 180 = West, 270 = South)
        val directionFromEast = (azimuthInDegrees - 90 + 360) % 360

        // Log the result for debugging purposes
        Log.d("Direction", "User is facing $directionFromEast degrees from East")

        // Return the direction in degrees relative to East
        return directionFromEast
    }

    // Remember to unregister the sensor listener when no longer needed (e.g., onPause)
    fun unregisterSensors() {
        sensorManager.unregisterListener(sensorEventListener)
    }
}
