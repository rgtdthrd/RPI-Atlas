package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import kotlin.math.pow
import kotlin.math.sqrt

class EdgeView(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    // Visual properties of the edge
    private val paint =
        Paint().apply {
            color = android.graphics.Color.RED
            strokeWidth = 10f
        }

    private lateinit var edge: Edge
    private lateinit var map: ImageView

    fun init(
        newEdge: Edge,
        mapImage: ImageView,
    ) {
        edge = newEdge
        map = mapImage
    }

    fun update(
        userLoc: Pair<Double, Double>,
        container: FrameLayout,
    ) {
        val (newSudoX, newSudoY) = getClosestPointOnEdge(userLoc)
        val distanceToEnd =
            sqrt(
                (userLoc.first - edge.end.position.first).pow(2) +
                    (userLoc.second - edge.end.position.second).pow(2),
            )
        // Determine if the edge has been traversed
        val threshold = 5.0
        if (distanceToEnd < threshold) {
            // Edge fully traversed
            edge.hide(container)
            Log.d("EdgeView", "Edge fully traversed")
        } else {
            edge.updateSudoNode(Pair(newSudoX, newSudoY))
        }
        // Redraw the view with updated edges
        invalidate()
    }

    // Function to get the closest point on the edge to the user's location
    private fun getClosestPointOnEdge(userLoc: Pair<Double, Double>): Pair<Double, Double> {
        val (x1, y1) = edge.start.position
        val (x2, y2) = edge.end.position
        val (px, py) = userLoc

        val edgeLengthSquared = (x2 - x1).pow(2) + (y2 - y1).pow(2)
        // If the edge is a single point
        if (edgeLengthSquared == 0.0) return Pair(x1, y1)

        val t = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / edgeLengthSquared
        val clampedT = t.coerceIn(0.0, 1.0)

        val closestX = x1 + clampedT * (x2 - x1)
        val closestY = y1 + clampedT * (y2 - y1)

        return Pair(closestX, closestY)
    }

    // Function to draw the edges on the map
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
            val end =
                convertLocation(
                    edge.end.position.first,
                    edge.end.position.second,
                )
            val startX = start.first * mapWidth / IMAGE_WIDTH
            val startY = start.second * mapHeight / IMAGE_HEIGHT
            val endX = end.first * mapWidth / IMAGE_WIDTH
            val endY = end.second * mapHeight / IMAGE_HEIGHT

            canvas.drawLine(startX, startY, endX, endY, paint)
        }
    }
}
