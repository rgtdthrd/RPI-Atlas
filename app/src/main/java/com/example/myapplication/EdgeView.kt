package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class EdgeView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val paint = Paint().apply {
        color = android.graphics.Color.RED  // Set your desired color
        strokeWidth = 5f  // Set your desired thickness
    }

    private val edges = ArrayList<Edge>()

    fun addEdge(newEdge: Edge) {
        edges.add(newEdge)
        invalidate()  // Redraw the view to display the new edge
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (edge in edges) {
            val start = ConvertLocation(edge.start.position.first, edge.start.position.second)
            val end = ConvertLocation(edge.end.position.first, edge.end.position.second)

            val startX = start.first.toFloat()
            val startY = start.second.toFloat()
            val endX = end.first.toFloat()
            val endY = end.second.toFloat()


            canvas.drawLine(startX, startY, endX, endY, paint)
        }
    }
}