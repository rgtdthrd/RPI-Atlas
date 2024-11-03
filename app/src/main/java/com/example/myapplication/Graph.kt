package com.example.myapplication

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

class Graph() {
    var nodes = emptyArray<Node>()
    // var edges = emptyArray<Edge>()

    fun AddNode(new_node: Node) {
        nodes += new_node
    }

    /*
    fun AddEdge(new_edge: Edge) {
        // edges += new_edge
    }
    */

    fun GetAllSearchableNodeNames(): Array<String> {
        var out_array = emptyArray<String>()
        for (node in nodes) {
            if (node is SearchableNode) {
                out_array += node.name
            }
        }
        return out_array
    }
    fun GetNodeByName(name: String): SearchableNode? {
        for (node in nodes) {
            if (node is SearchableNode && node.name.equals(name, ignoreCase = true)) {
                return node
            }
        }
        return null
    }

    fun ParseLandmarksFromCSV(context: Context, resourceId: Int): List<SearchableNode> {
        val nodeList = mutableListOf<SearchableNode>()
        val inputStream = context.resources.openRawResource(resourceId)
        val reader = BufferedReader(InputStreamReader(inputStream))

        try {
            var line: String?
            reader.readLine()  // skip the title line
            while (reader.readLine().also { line = it } != null) {
                line?.let {
                    val columns = it.split(",")
                    assert(columns.size == 3)
                    val name = columns[0]
                    val lat = columns[1]
                    val long = columns[2]
                    val tmp = SearchableNode(position = Pair(lat.toDouble(), long.toDouble()), name = name)
                    nodeList.add(tmp)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            reader.close()
        }

        return nodeList
    }


    fun ParseNodesFromCSV(context: Context, resourceId: Int) {
        val inputStream = context.resources.openRawResource(resourceId)
        val reader = BufferedReader(InputStreamReader(inputStream))

        try {
            var line: String?
            reader.readLine()
            while (reader.readLine().also { line = it } != null) {
                line?.let {
                    val columns = it.split(",")
                    assert(columns.size == 3)
                    val name = columns[0]
                    val lat = columns[1]
                    val long = columns[2]
                    val node = Node(position = Pair(lat.toDouble(), long.toDouble()), name = name)
                    AddNode(node)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            reader.close()
        }
    }


    // Edge class is not implemented yet, but this should work when it is implemented and the two lines are uncommented.
    fun ParseEdgesFromCSV(context: Context, resourceId: Int){
        val inputStream = context.resources.openRawResource(resourceId)
        val reader = BufferedReader(InputStreamReader(inputStream))
        try {
            var line: String?
            reader.readLine()
            while (reader.readLine().also { line = it } != null) {
                line?.let {
                    val columns = it.split(",")
                    assert(columns.size == 2)
                    val nodeName1 = columns[0]
                    val nodeName2 = columns[1]
                    val start = GetNodeByName(nodeName1)
                    val end = GetNodeByName(nodeName2)
                    assert(start != null)
                    assert(end != null)
                    // possibly add weight here too
                    // val edge = Edge(start = start, end = end)
                    // AddEdge(edge)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            } finally {
            reader.close()
        }

    }



}

open class Node(val position: Pair<Double, Double>, val name: String) {

}

open class SearchableNode(position: Pair<Double, Double>, name: String) : Node(position, name) {

}