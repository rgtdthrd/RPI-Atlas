package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import kotlin.math.*

class EdgeView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val paint = Paint().apply {
        color = android.graphics.Color.RED  // Set your desired color
        strokeWidth = 10f  // Set your desired thickness
    }

    private val edges = ArrayList<Edge>()
    private var map: ImageView? = null  // Private attribute for the map

    // Method to set the map
    fun setMap(imageView: ImageView) {
        map = imageView
    }

    fun addEdge(newEdge: Edge) {
        edges.add(newEdge)
        invalidate()  // Redraw the view to display the new edge
    }
    private fun getClosestPointOnEdge(edge: Edge, userLoc: Pair<Double, Double>): Pair<Double, Double> {
        val (x1, y1) = edge.start.position
        val (x2, y2) = edge.end.position
        val (px, py) = userLoc

        val edgeLengthSquared = (x2 - x1).pow(2) + (y2 - y1).pow(2)
        if (edgeLengthSquared == 0.0) return Pair(x1, y1)  // If the edge is a single point

        val t = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / edgeLengthSquared
        val clampedT = t.coerceIn(0.0, 1.0)

        val closestX = x1 + clampedT * (x2 - x1)
        val closestY = y1 + clampedT * (y2 - y1)

        return Pair(closestX, closestY)
    }
    fun update(userLoc: Pair<Double, Double>) {
        val iterator = edges.iterator()
        while (iterator.hasNext()) {
            val edge = iterator.next()

            val (newSudoX, newSudoY) = getClosestPointOnEdge(edge, userLoc)
            val distanceToEnd = sqrt((userLoc.first - edge.end.position.first).pow(2) +
                    (userLoc.second - edge.end.position.second).pow(2))

            // determine if the edge has been traversed
            val threshold = 5.0
            if (distanceToEnd < threshold) {
                iterator.remove()  // remove the edge if it has been traversed
            } else {
                // update the sudo node position
                edge.updateSudoNode(Pair(newSudoX, newSudoY), "UpdatedSudo")
            }
        }
        invalidate()  // 重新绘制视图以更新边
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        map?.let { mapView ->
            val mapWidth = mapView.width.toFloat()
            val mapHeight = mapView.height.toFloat()

            for (edge in edges) {
                val start = ConvertLocation(edge.getSudoNode().position.first, edge.getSudoNode().position.second)
                val end = ConvertLocation(edge.end.position.first, edge.end.position.second)

                val startX = start.first.toFloat() * mapWidth / IMAGE_WIDTH
                val startY = start.second.toFloat() * mapHeight / IMAGE_HEIGHT
                val endX = end.first.toFloat() * mapWidth / IMAGE_WIDTH
                val endY = end.second.toFloat() * mapHeight / IMAGE_HEIGHT

                canvas.drawLine(startX, startY, endX, endY, paint)
            }
        }
    }
}