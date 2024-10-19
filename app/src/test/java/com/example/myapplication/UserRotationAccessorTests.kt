
import android.content.Context
import android.hardware.SensorManager
import com.example.myapplication.OrientationHelper
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OrientationHelperTest {

    private lateinit var context: Context
    private lateinit var orientationHelper: OrientationHelper
    private lateinit var sensorManager: SensorManager

    @Before
    fun setup() {
        // Create a mock context
        context = mockk(relaxed = true)

        // Mock the SensorManager
        sensorManager = mockk(relaxed = true)

        every { context.getSystemService(Context.SENSOR_SERVICE) } returns sensorManager

        // Initialize the com.example.myapplication.OrientationHelper with the mocked context
        orientationHelper = OrientationHelper(context)
    }

    @Test
    fun testGetUserRotationEast() {
        // Simulate sensor values for East direction (azimuth = 90°)
        val azimuthInDegrees = 90f
        val accelerometerValues = floatArrayOf(0f, 0f, 9.81f) // Example accelerometer values
        val magnetometerValues = floatArrayOf(1f, 0f, 0f) // Example magnetometer values

        // Call the function to get the user's facing direction
        val direction = orientationHelper.getUserRotation()

        // Assert that the returned direction from East is correct
        assertEquals(0f, direction)
    }

    @Test
    fun testGetUserRotationNorth() {
        // Simulate sensor values for North direction (azimuth = 0°)
        val azimuthInDegrees = 0f
        val accelerometerValues = floatArrayOf(0f, 0f, 9.81f) // Example accelerometer values
        val magnetometerValues = floatArrayOf(1f, 0f, 0f) // Example magnetometer values

        // Call the function to get the user's facing direction
        val direction = orientationHelper.getUserRotation()

        // Assert that the returned direction from East is correct
        assertEquals(90f, direction)
    }

}
