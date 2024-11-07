package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView

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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        map?.let { mapView ->
            val mapWidth = mapView.width.toFloat()
            val mapHeight = mapView.height.toFloat()

            for (edge in edges) {
                val start = ConvertLocation(edge.start.position.first, edge.start.position.second)
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