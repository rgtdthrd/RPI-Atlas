package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import kotlin.math.*

class EdgeView(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val paint =
        Paint().apply {
            color = android.graphics.Color.RED // Set your desired color
            strokeWidth = 10f // Set your desired thickness
        }

    private lateinit var edge: Edge
    private lateinit var map: ImageView
    fun init(newEdge: Edge, mapImage: ImageView) {
        edge = newEdge
        map = mapImage
    }
    fun update(userLoc: Pair<Double, Double>, container: FrameLayout) {
        val (newSudoX, newSudoY) = getClosestPointOnEdge(userLoc)
        val distanceToEnd = sqrt((userLoc.first - edge.end.position.first).pow(2) +
                (userLoc.second - edge.end.position.second).pow(2))
        // Determine if the edge has been traversed
        val threshold = 5.0
        if (distanceToEnd < threshold) {
            // Edge fully traversed; handle edge completion logic if necessary
            edge.hide(container)
            Log.d("EdgeView", "Edge fully traversed")
        } else {
            edge.updateSudoNode(Pair(newSudoX, newSudoY))

        }
        invalidate()  // Redraw the view with updated edges
    }
    private fun getClosestPointOnEdge(
        userLoc: Pair<Double, Double>,
    ): Pair<Double, Double> {
        val (x1, y1) = edge.start.position
        val (x2, y2) = edge.end.position
        val (px, py) = userLoc

        val edgeLengthSquared = (x2 - x1).pow(2) + (y2 - y1).pow(2)
        if (edgeLengthSquared == 0.0) return Pair(x1, y1) // If the edge is a single point

        val t = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / edgeLengthSquared
        val clampedT = t.coerceIn(0.0, 1.0)

        val closestX = x1 + clampedT * (x2 - x1)
        val closestY = y1 + clampedT * (y2 - y1)

        return Pair(closestX, closestY)
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        map.let { mapView ->
            val mapWidth = mapView.width.toFloat()
            val mapHeight = mapView.height.toFloat()
            val start =
                convertLocation(
                    edge.getSudoNode().position.first,
                    edge.getSudoNode().position.second,
                )
            val end = convertLocation(
                edge.end.position.first,
                edge.end.position.second
            )
            val startX = start.first.toFloat() * mapWidth / IMAGE_WIDTH
            val startY = start.second.toFloat() * mapHeight / IMAGE_HEIGHT
            val endX = end.first.toFloat() * mapWidth / IMAGE_WIDTH
            val endY = end.second.toFloat() * mapHeight / IMAGE_HEIGHT

            canvas.drawLine(startX, startY, endX, endY, paint)
        }
    }
}
