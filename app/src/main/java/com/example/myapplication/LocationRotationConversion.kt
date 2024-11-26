package com.example.myapplication

import android.util.Log
import android.widget.ImageView

private const val MAP_ORIENTATION_OFFSET = 0.0

// sample data for real latitude/longitude positions
private val LOCPOS =
    arrayOf(
        Pair(42.730495, -73.678432),
        Pair(42.73236423119569, -73.67009163262247),
        Pair(42.72706080812248, -73.67660537697532),
        Pair(42.730149231745465, -73.6814722452884),
        Pair(42.72908213598602, -73.67265224109543),
        Pair(42.72845472653638, -73.68341858852392),
        Pair(42.73078307416792, -73.67722000350271),
        Pair(42.72884496556526, -73.68302650605078),
        Pair(42.72688623007297, -73.6736111308174),
    )

// sample data for image x/y positions
private val LOCMAP =
    arrayOf(
        Pair(587, 749),
        Pair(1121, 584),
        Pair(707, 1047),
        Pair(395, 780),
        Pair(961, 868),
        Pair(274, 925),
        Pair(668, 724),
        Pair(296, 893),
        Pair(899, 1061),
    )

// number of sample points
private val TESTSIZE = LOCPOS.size

// number of combinations of sample points
private val COMBOS: Int = TESTSIZE * (TESTSIZE - 1) / 2

// The scale factor from latitude/longitude to pixels
private var scaleFactor = Pair(0.0, 0.0)

private var referenceLocMap = Pair(0.0, 0.0)
private var referenceLocPos = Pair(0.0, 0.0)
const val IMAGE_WIDTH = 1582
const val IMAGE_HEIGHT = 1285
const val X_OFFSET = -9.0f
const val Y_OFFSET = -5.0f

// retrieve the sample points
private fun getReferencePoints() {
    val locmapToDouble =
        Array(TESTSIZE) { i -> LOCMAP[i].first.toDouble() to LOCMAP[i].second.toDouble() }
    referenceLocMap = meanPoint(locmapToDouble)
    referenceLocPos = meanPoint(LOCPOS)
}

// calculate the scale factor
private fun getScaleFactor() {
    /*val LOCMAP_to_Double = Array(TESTSIZE) { i -> LOCMAP[i].first.toDouble() to LOCMAP[i].second.toDouble() }
    val delta1 = DeltaDistance(LOCMAP_to_Double)
    val delta2 = DeltaDistance(LOCPOS)
    var total_point = emptyArray<Pair<Double, Double>>()
    for (i in 0 until COMBOS) {
        total_point += Pair(delta1[i].first / delta2[i].second, delta1[i].second / delta2[i].first)
    }
    scale_factor = MeanPoint(total_point)*/
    scaleFactor = Pair(62828.4066235, -86993.3520537)
}

private fun dotProduct(
    a: Pair<Double, Double>,
    b: Pair<Double, Double>,
): Double = a.first * b.first + a.second * b.second

// calculate every distance combination in the sample points
private fun deltaDistance(mypoints: Array<Pair<Double, Double>>): Array<Pair<Double, Double>> {
    val delta = Array(COMBOS) { Pair(0.0, 0.0) }
    var index = 0
    for (i in 0 until TESTSIZE - 1) {
        for (j in i + 1 until TESTSIZE) {
            delta[index] =
                Pair(mypoints[i].first - mypoints[j].first, mypoints[i].second - mypoints[j].second)
            index++
        }
    }
    return delta
}

// calculate the mean of an array of points
private fun meanPoint(mypoints: Array<Pair<Double, Double>>): Pair<Double, Double> {
    var meanX = 0.0
    var meanY = 0.0
    for (point in mypoints) {
        meanX += point.first
        meanY += point.second
    }
    meanX /= mypoints.size
    meanY /= mypoints.size
    return Pair(meanX, meanY)
}

// convert latitude/longitude to pixels
fun convertLocation(
    latitude: Double,
    longitude: Double,
): Pair<Float, Float> {
    if (scaleFactor == Pair(0.0, 0.0)) {
        getScaleFactor()
    }
    if (referenceLocMap == Pair(0.0, 0.0) || referenceLocPos == Pair(0.0, 0.0)) {
        getReferencePoints()
    }
    val newX =
        (longitude - referenceLocPos.second) * scaleFactor.first + referenceLocMap.first
    val newY =
        (latitude - referenceLocPos.first) * scaleFactor.second + referenceLocMap.second
    return Pair(newX.toFloat(), newY.toFloat())
}

fun convertRotation(cardinalRotation: Double): Double = (cardinalRotation - MAP_ORIENTATION_OFFSET) % 360

fun displayLocation(
    map: ImageView,
    marker: ImageView,
    xPos: Float,
    yPos: Float,
) {
    marker.visibility = ImageView.VISIBLE

    val markerXScale = map.width.toFloat() / IMAGE_WIDTH.toFloat()
    val markerYScale = map.height.toFloat() / IMAGE_HEIGHT.toFloat()
    val mapScaleX = markerXScale * IMAGE_WIDTH.toFloat()
    val mapScaleY = markerYScale * IMAGE_HEIGHT.toFloat()

    if (markerXScale in 0.0f..mapScaleX && markerYScale in 0.0f..mapScaleY) {
        marker.x = markerXScale * xPos + X_OFFSET
        marker.y = markerYScale * yPos + Y_OFFSET
        Log.d("DisplayLocation", "Marker placed at: $xPos, $yPos")
    } else {
        Log.d("DisplayLocation", "Marker position out of bounds: $xPos, $yPos")
    }
}

fun displayRotation(
    map: ImageView,
    arrow: ImageView,
    degrees: Double,
) {
    /*
    Needed Direction: right = 0.0, up = 90.0, left = 180.0, down = 270.0
    Given Direction (degrees): 270 = right, 180 = up, 90 = left, 0.0 = down
    Equation: ((direction) - 360) + 90 = correct direction
     */

    arrow.visibility = ImageView.VISIBLE
    arrow.rotation = degrees.toFloat() - 90.0f // abs((degrees.toFloat() - 360.0f) + 90.0f)
    // Log.d("DisplayRotation", "Arrow rotated to: $degrees degrees")
}
