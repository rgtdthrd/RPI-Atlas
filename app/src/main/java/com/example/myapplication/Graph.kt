package com.example.myapplication

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

class Graph() {
    var nodes = emptyArray<Node>()
    var edges = emptyArray<Edge>()

    fun AddNode(new_node: Node) {
        nodes += new_node
    }

    fun AddEdge(new_edge: Edge) {
        edges += new_edge
    }


    fun GetAllLandmarkNodeNames(): Array<String> {
        var out_array = emptyArray<String>()
        for (node in nodes) {
            if (node is LandmarkNode) {
                out_array += node.name
            }
        }
        return out_array
    }
    fun GetNodeByName(name: String): Node? {
        for (node in nodes) {
            if (node.name.equals(name, ignoreCase = true)) {
                return node
            }
        }
        return null
    }

    fun GetLandmarkNodeByName(name: String): LandmarkNode? {
        for (node in nodes) {
            if (node is LandmarkNode && node.name.equals(name, ignoreCase = true)) {
                return node
            }
        }
        return null
    }

    fun ParseLandmarksFromCSV(context: Context, resourceId: Int): List<LandmarkNode> {
        val nodeList = mutableListOf<LandmarkNode>()
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
                    val tmp = LandmarkNode(position = Pair(lat.toDouble(), long.toDouble()), name = name)
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
                    if (start != null && end != null) {
                        val edge = Edge(start = start, end = end)
                        AddEdge(edge)
                    } else {
                        // Handle the case where a node was not found
                        println("Error: One or both of the nodes were not found.")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            } finally {
            reader.close()
        }

    }

    fun GetClosestNode(position: Pair<Double, Double>): Node? {
        if (nodes.isEmpty()) return null  // Return null if there are no nodes in the graph
        var closestNode: Node? = null
        var minDistance = Double.POSITIVE_INFINITY
        // check all nodes for distance, then return the closest one
        for (node in nodes) {
            val distance = CalculateDistance(position, node.position)
            if (distance < minDistance) {
                minDistance = distance
                closestNode = node
            }
        }
        return closestNode
    }

    //Dijkstra's to find shortest path
    fun ShortestPath(start_node: Node, end_node: Node): Route {
        val distances = mutableMapOf<Node, Double>().withDefault { Double.POSITIVE_INFINITY }
        val previousNodes = mutableMapOf<Node, Edge?>()
        val visited = mutableSetOf<Node>()
        val priorityQueue = java.util.PriorityQueue(compareBy<Pair<Node, Double>> { it.second })

        // Set the initial distance to the starting node as 0
        distances[start_node] = 0.0
        priorityQueue.add(Pair(start_node, 0.0))

        while (priorityQueue.isNotEmpty()) {
            // the !! asserts that its not null
            val (currentNode, currentDistance) = priorityQueue.poll()!!

            // Skip if already visited
            if (visited.contains(currentNode)) continue
            visited.add(currentNode)

            // Stop if we've reached the end node
            if (currentNode == end_node) break

            // Relax edges from the current node
            for (edge in edges) {
                if (edge.start == currentNode) {
                    val neighbor = edge.end
                    if (!visited.contains(neighbor)) {
                        val newDistance = currentDistance + edge.weight
                        if (newDistance < distances.getValue(neighbor)) {
                            distances[neighbor] = newDistance
                            previousNodes[neighbor] = edge
                            priorityQueue.add(Pair(neighbor, newDistance))
                        }
                    }
                }
            }
        }

        // Reconstruct the path
        val route = Route()
        var currentNode: Node? = end_node
        while (currentNode != null && previousNodes[currentNode] != null) {
            val edge = previousNodes[currentNode]
            if (edge != null) {
                route.addEdge(edge)
                currentNode = edge.start
            }
        }

        // Reconstruct the path and reverse it since we built it backwards
        val reversedEdges = route.getEdges().toMutableList()
        reversedEdges.reverse()
        route.setEdges(reversedEdges)
        return route
    }





}

//should hold all nodes, landmarks and others
open class Node(val position: Pair<Double, Double>, val name: String) {

}

//only landmarks
open class LandmarkNode(position: Pair<Double, Double>, name: String) : Node(position, name) {

}

open class Edge(val start: Node, val end: Node) {
    val weight: Double = CalculateDistance(start.position, end.position)
}

open class Route {
    private val edges: MutableList<Edge> = mutableListOf()

    fun addEdge(edge: Edge) {
        edges.add(edge)
    }

    fun getEdges(): List<Edge> {
        return edges
    }

    fun getTotalWeight(): Double {
        return edges.sumOf { it.weight }
    }

    fun setEdges(newEdges: List<Edge>) {
        edges.clear()
        edges.addAll(newEdges)
    }

    override fun toString(): String {
        return edges.joinToString(separator = " -> ") { "${it.start.name} to ${it.end.name} (Weight: ${it.weight})" }
    }
}
