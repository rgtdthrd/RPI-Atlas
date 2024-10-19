import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import android.hardware.SensorManager

class OrientationHelperTest {

    private lateinit var orientationHelper: OrientationHelper

    @Before
    fun setUp() {
        // You can pass a mock Context or SensorManager here if needed
        val context = mock(Context::class.java)
        orientationHelper = OrientationHelper(context)
    }

    @Test
    fun testGetUserRotation() {
        // Manually set the mock sensor readings or call the GetUserRotation function directly
        val result = orientationHelper.GetUserRotation()

        // Test against expected values
        assertTrue(result in 0.0..360.0)  // Check if result is a valid angle
    }
}
