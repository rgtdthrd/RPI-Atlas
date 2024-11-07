package com.example.myapplication
import android.media.Image
import android.widget.ImageView
import android.util.Log
import kotlin.math.*

private val MAP_ORIENTATION_OFFSET = 0.0

private val LOCPOS = arrayOf(
    Pair(42.730495,-73.678432),
    Pair(42.73236423119569,-73.67009163262247),
    Pair(42.72706080812248,-73.67660537697532),
    Pair(42.730149231745465,-73.6814722452884),
    Pair(42.72908213598602,-73.67265224109543),
    Pair(42.72845472653638,-73.68341858852392),
    Pair(42.73078307416792,-73.67722000350271),
    Pair(42.72884496556526,-73.68302650605078),
    Pair(42.72688623007297,-73.6736111308174)
)

private val LOCMAP = arrayOf(
    Pair(587,749),
    Pair(1121,584),
    Pair(707,1047),
    Pair(395,780),
    Pair(961,868),
    Pair(274,925),
    Pair(668,724),
    Pair(296,893),
    Pair(899,1061)
)


private val TESTSIZE = LOCPOS.size
private val COMBOS: Int = TESTSIZE * (TESTSIZE - 1) / 2
private var scale_factor = Pair(0.0, 0.0)
private var reference_loc_map = Pair(0.0, 0.0)
private var reference_loc_pos = Pair(0.0, 0.0)
private val IMAGE_WIDTH = 1582
private val IMAGE_HEIGHT = 1285
private val X_OFFSET = 1278
private val Y_OFFSET = 55

private fun get_reference_points() {
    val LOCMAP_to_Double = Array(TESTSIZE) { i -> LOCMAP[i].first.toDouble() to LOCMAP[i].second.toDouble() }
    reference_loc_map = MeanPoint(LOCMAP_to_Double)
    reference_loc_pos = MeanPoint(LOCPOS)
}

private fun get_scale_factor() {
    /*val LOCMAP_to_Double = Array(TESTSIZE) { i -> LOCMAP[i].first.toDouble() to LOCMAP[i].second.toDouble() }
    val delta1 = DeltaDistance(LOCMAP_to_Double)
    val delta2 = DeltaDistance(LOCPOS)
    var total_point = emptyArray<Pair<Double, Double>>()
    for (i in 0 until COMBOS) {
        total_point += Pair(delta1[i].first / delta2[i].second, delta1[i].second / delta2[i].first)
    }
    scale_factor = MeanPoint(total_point)*/
    scale_factor = Pair(62828.4066235, -86993.3520537)

}

private fun DotProduct(a: Pair<Double, Double>, b: Pair<Double, Double>): Double {
    return a.first * b.first + a.second * b.second
}

private fun Magnitude(mypoint: Pair<Double, Double>): Double {
    return sqrt(DotProduct(mypoint, mypoint))
}

private fun DeltaDistance(mypoints: Array<Pair<Double, Double>>): Array<Pair<Double, Double>> {
    val delta = Array(COMBOS) { Pair(0.0, 0.0) }
    var index = 0
    for (i in 0 until TESTSIZE - 1) {
        for (j in i + 1 until TESTSIZE) {
            delta[index] = Pair(mypoints[i].first - mypoints[j].first, mypoints[i].second - mypoints[j].second)
            index++
        }
    }
    return delta
}

private fun MeanPoint(mypoints: Array<Pair<Double, Double>>): Pair<Double, Double> {
    var mean_x = 0.0
    var mean_y = 0.0
    for (point in mypoints) {
        mean_x += point.first
        mean_y += point.second
    }
    mean_x /= mypoints.size
    mean_y /= mypoints.size
    return Pair(mean_x, mean_y)
}

private fun RotatePoint(mypoint: Pair<Double, Double>, angle: Double): Pair<Double, Double> {
    val x = mypoint.first * cos(angle) - mypoint.second * sin(angle)
    val y = mypoint.first * sin(angle) + mypoint.second * cos(angle)
    return Pair(x, y)
}

fun ConvertLocation(latitude: Double, longitude: Double): Pair<Int, Int> {
    if (scale_factor == Pair(0.0, 0.0)) {
        get_scale_factor()
    }
    if (reference_loc_map == Pair(0.0, 0.0) || reference_loc_pos == Pair(0.0, 0.0)) {
        get_reference_points()
    }
    val new_x = (longitude - reference_loc_pos.second) * scale_factor.first + reference_loc_map.first
    val new_y = (latitude - reference_loc_pos.first) * scale_factor.second + reference_loc_map.second
    return Pair(new_x.toInt(), new_y.toInt())
}

fun ConvertRotation(cardinal_rotation: Double): Double {
    return (cardinal_rotation - MAP_ORIENTATION_OFFSET) % 360
}

fun DisplayLocation(map: ImageView, marker: ImageView, xPos: Int, yPos: Int) {

    marker.visibility = ImageView.VISIBLE

    val markerXScale = map.width.toFloat() / IMAGE_WIDTH.toFloat()
    val markerYScale = map.height.toFloat() / IMAGE_HEIGHT.toFloat()
    val mapScaleX = markerXScale.toInt() * IMAGE_WIDTH + 1
    val mapScaleY = markerYScale.toInt() * IMAGE_HEIGHT + 1

    if (markerXScale.toInt() in 0..mapScaleX && markerYScale.toInt() in 0..mapScaleY) {
        marker.x = markerXScale * xPos.toFloat()// - marker.width / 2
        marker.y = markerYScale * yPos.toFloat()// - marker.height / 2
        Log.d("DisplayLocation", "Marker placed at: $xPos, $yPos")
    } else {
        Log.d("DisplayLocation", "Marker position out of bounds: $xPos, $yPos")
    }
}

fun DisplayRotation(map: ImageView, arrow: ImageView, degrees: Double) {
    /*
    Needed Direction: right = 0.0, up = 90.0, left = 180.0, down = 270.0
    Given Direction (degrees): 270 = right, 180 = up, 90 = left, 0.0 = down
    Equation: ((direction) - 360) + 90 = correct direction
     */

    arrow.visibility = ImageView.VISIBLE
    arrow.rotation = degrees.toFloat() - 90.0f // abs((degrees.toFloat() - 360.0f) + 90.0f)
    //Log.d("DisplayRotation", "Arrow rotated to: $degrees degrees")

}