package com.example.myapplication

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView


class DisplayLandmarkNames{

    fun displayLandmarkNames(context: Context, landmarkGraph: Graph, parentLayout: ViewGroup) {
        for (node in landmarkGraph.nodes) {
            if (node is LandmarkNode) {
                val textView = TextView(context)
                val (x, y) = convertLocation(node.position.first, node.position.second)

                textView.text = node.name
                textView.x = x
                textView.y = y
                parentLayout.addView(textView)

            }

        }
    }

}