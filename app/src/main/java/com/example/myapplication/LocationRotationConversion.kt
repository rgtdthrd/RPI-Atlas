package com.example.myapplication
import kotlin.math.*

private val LOCPOS = arrayOf(
    Pair(42.730495, -73.678432),
    Pair(42.73236423119569, -73.67009163262247),
    Pair(42.72706080812248, -73.67660537697532),
    Pair(42.730149231745465, -73.6814722452884),
    Pair(42.72795853984359, -73.67820780693468),
    Pair(42.72845472653638, -73.68341858852392),
    Pair(42.73078307416792, -73.67722000350271)
)

private val LOCMAP = arrayOf(
    Pair(587, 749),
    Pair(1121, 584),
    Pair(707, 1047),
    Pair(395, 780),
    Pair(604, 970),
    Pair(274, 925),
    Pair(668, 724)
)

private val TESTSIZE = LOCPOS.size
private val COMBOS: Int = TESTSIZE * (TESTSIZE - 1) / 2
private var scale_factor = 0.0
private var radian_displace = 0.0

//private fun get_scale_factor(): Double

//private fun get_radian_displace(): Double

//private fun dot_product(a: Pair<Double, Double>, b: Pair<Double, Double>): Double

//private fun magnitute(mypoint: Pair<Double, Double>): Double

//private fun distance_differences(mypoints: Pair<Double, Double>): Array<Pair<Double, Double>>

//private fun mean_point(mypoints: Pair<Double, Double>): Pair<Double, Double>

//private fun rotate_point(mypoint: Pair<Double, Double>, angle: Double): Pair<Double, Double>

//fun ConvertLocation(latitude: Double, longitude: Double): Pair<Double, Double>
