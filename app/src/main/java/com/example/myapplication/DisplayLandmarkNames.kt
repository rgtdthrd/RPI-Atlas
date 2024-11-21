package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.ImageView
import android.view.View
import android.util.Log
import android.graphics.Typeface


// Class to display landmark names on the map

class DisplayLandmarkNames(context: Context, attrs: AttributeSet? = null) : View(context, attrs){
    private val paint = Paint().apply {
        color = android.graphics.Color.argb(150, 0, 0, 200)
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 1.5f
        textSize = 18f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
    }


    private lateinit var map: ImageView
    private lateinit var graph: Graph

    fun init(newMap: ImageView, newGraph: Graph) {
        map = newMap
        graph = newGraph
    }

    override fun onDraw(canvas: Canvas){
        super.onDraw(canvas)
        val mapWidth = map.width.toFloat()
        val mapHeight = map.height.toFloat()

        // List of landmarks that are already displayed on the map image and do not need to be displayed again
        val preDisplayedLandmarks = listOf("86 Field", "Harkness Field", "Stadium Field", "Samaritan Hospital")

        for(node in graph.nodes){
            if(node is LandmarkNode && node.name !in preDisplayedLandmarks){
                // Receive converted coordinates from landmark and draw on the map with
                // scaled coordinates
                val (rawX, rawY) = convertLocation(node.position.first, node.position.second)
                val aX = mapWidth / IMAGE_WIDTH.toFloat()
                val aY = mapHeight / IMAGE_HEIGHT.toFloat()

                val scaledX = aX * rawX - 45
                val scaledY = aY * rawY

                val landmarkName = node.name

                // For longer names, the text is broken up into two lines for readability
                var textMessageA = landmarkName
                var textMessageB = ""
                if(landmarkName.length >= 15 && landmarkName.contains(" ") ){
                    if(landmarkName.count{it == ' '} >= 2 &&
                        (!landmarkName.contains("center for", ignoreCase = true))){
                        val splitLandmark = landmarkName.split(" ", limit = 3)
                        textMessageA = splitLandmark[0] + " " + splitLandmark[1]
                        textMessageB = splitLandmark[2]

                    }else if(landmarkName.count{it == ' '} >= 2 &&
                        (landmarkName.contains("center for", ignoreCase = true))) {
                        val splitLandmark = landmarkName.split(" ", limit = 4)
                        textMessageA = splitLandmark[0] + " " + splitLandmark[1] + " " + splitLandmark[2]
                        textMessageB = splitLandmark[3]

                    }else{
                        val splitLandmark = landmarkName.split(" ", limit = 2)
                        textMessageA = splitLandmark[0]
                        textMessageB = splitLandmark[1]
                    }
                    canvas.drawText(textMessageA, scaledX, scaledY, paint)
                    canvas.drawText(textMessageB, scaledX + 8, scaledY + 30, paint)

                // Otherwise, just draw the text normally
                }else{
                    canvas.drawText(landmarkName, scaledX, scaledY, paint)
                }

            }
        }

    }


}