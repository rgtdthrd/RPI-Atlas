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
        color = android.graphics.Color.argb(200, 0, 0, 200)
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

    // For longer names, the text is broken up into two lines for improved readability
    // This function splits the text depending on its contents
    private fun splitLandmarkText(landmarkName: String): Pair<String, String> {
        return when {
            landmarkName.count { it == ' ' } >= 2 &&
                    !landmarkName.contains("center for", ignoreCase = true) -> {
                val split = landmarkName.split(" ", limit = 3)
                Pair("${split[0]} ${split[1]}", split[2])
            }
            landmarkName.count { it == ' ' } >= 2 &&
                    landmarkName.contains("center for", ignoreCase = true) -> {
                val split = landmarkName.split(" ", limit = 4)
                Pair("${split[0]} ${split[1]} ${split[2]}", split[3])
            }
            else -> {
                val split = landmarkName.split(" ", limit = 2)
                Pair(split[0], split[1])
            }
        }
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

                // Display text depending on length and structure of landmark name
                if(landmarkName.length >= 15 && landmarkName.contains(" ") ){
                    val (textMessageA, textMessageB) = splitLandmarkText(landmarkName)
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