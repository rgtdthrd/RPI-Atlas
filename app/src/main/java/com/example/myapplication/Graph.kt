package com.example.myapplication

import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import java.io.BufferedReader
import java.io.InputStreamReader

// Graph structure to represent the map of RPI campus
// where nodes are locations on campus and edges are roads or pathways between locations
class Graph {
    var nodes = emptyArray<Node>()
    private var edges = emptyArray<Edge>()
    private var currentRoute: Route? = null

    fun addNode(newNode: Node) {
        nodes += newNode
    }

    fun addEdge(newEdge: Edge) {
        edges += newEdge
    }

    fun getAllEdges(): Array<Edge> = edges

    // Get the names of all landmark nodes
    fun getAllLandmarkNodeNames(): Array<String> {
        var outArray = emptyArray<String>()
        for (node in nodes) {
            if (node is LandmarkNode) {
                outArray += node.name
            }
        }
        return outArray
    }

    // Get the node with the given name
    fun getNodeByName(name: String): Node? {
        for (node in nodes) {
            if (node.name.equals(name, ignoreCase = true)) {
                return node
            }
        }
        return null
    }

    // Get the landmark node with the given name
    fun getLandmarkNodeByName(name: String): LandmarkNode? {
        for (node in nodes) {
            if (node is LandmarkNode && node.name.equals(name, ignoreCase = true)) {
                return node
            }
        }
        return null
    }

    // Retrieve landmarks from CSV file
    fun parseLandmarksFromCSV(
        context: Context,
        resourceId: Int,
    ): List<LandmarkNode> {
        val nodeList = mutableListOf<LandmarkNode>()
        val inputStream = context.resources.openRawResource(resourceId)
        val reader = BufferedReader(InputStreamReader(inputStream))

        try {
            var line: String?

            // skip the title line
            reader.readLine()
            while (reader.readLine().also { line = it } != null) {
                line?.let {
                    val columns = it.split(",")
                    assert(columns.size == 3)
                    val name = columns[0]
                    val lat = columns[1]
                    val long = columns[2]
                    val tmp =
                        LandmarkNode(position = Pair(lat.toDouble(), long.toDouble()), name = name)
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

    // Parses non-landmark nodes and adds them to the graph from the nodedata.csv file
    fun parseNodesFromCSV(
        context: Context,
        resourceId: Int,
    ) {
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
                    addNode(node)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            reader.close()
        }
    }

    // Parses edges and adds them to the graph from the edgedata.csv file
    fun parseEdgesFromCSV(
        context: Context,
        resourceId: Int,
    ) {
        val inputStream = context.resources.openRawResource(resourceId)
        val reader = BufferedReader(InputStreamReader(inputStream))
        try {
            var line: String?
            reader.readLine()
            while (reader.readLine().also { line = it } != null) {
                line?.let {
                    val columns = it.split(",")
                    assert(columns.size == 3)
                    val nodeName1 = columns[0]
                    val nodeName2 = columns[1]
                    val accessible = columns[2]
                    val start = getNodeByName(nodeName1)
                    val end = getNodeByName(nodeName2)
                    if (start != null && end != null) {
                        val edge = Edge(start = start, end = end, accessible = accessible)
                        addEdge(edge)
                    } else {
                        // Handle the case where a node was not found
                        Log.d("Graph", "This is nodeName1: $nodeName1 and nodeName2: $nodeName2")
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

    // Find the closest node to a given position
    fun getClosestNode(position: Pair<Double, Double>): Node? {
        if (nodes.isEmpty()) return null // Return null if there are no nodes in the graph
        var closestNode: Node? = null
        var minDistance = Double.POSITIVE_INFINITY
        // check all nodes for distance, then return the closest one
        for (node in nodes) {
            val distance = calculateDistance(position, node.position)
            if(distance < 0.0){
                Log.d("Graph", "Distance for node ${node.name} is negative: $distance")
            }
            if (distance < minDistance) {
                minDistance = distance
                closestNode = node
            }
        }
        return closestNode
    }

    // Dijkstra's to find shortest path between 2 nodes already on the graph (use get closest node if necessary)
    fun shortestPath(
        startNode: Node,
        endNode: Node,
    ): Route {
        // Maps to store the shortest distance from the startNode to each node.
        val distances = mutableMapOf<Node, Double>().withDefault { Double.POSITIVE_INFINITY }
        // Maps to keep track of the previous node (or edge) in the shortest path to reconstruct it later.
        val previousNodes = mutableMapOf<Node, Edge?>()
        // Set of nodes already visited to prevent reprocessing.
        val visited = mutableSetOf<Node>()
        // Priority queue to process nodes in the order of their current shortest distance.
        val priorityQueue = java.util.PriorityQueue(compareBy<Pair<Node, Double>> { it.second })

        // Initialize the start node with a distance of 0.
        distances[startNode] = 0.0
        priorityQueue.add(Pair(startNode, 0.0))

        // Dijkstra's algorithm main loop: process nodes until the queue is empty.
        while (priorityQueue.isNotEmpty()) {
            // Dequeue the node with the smallest distance.
            val (currentNode, currentDistance) = priorityQueue.poll()!!

            // Skip processing if this node was already visited.
            if (visited.contains(currentNode)) continue
            visited.add(currentNode)

            // If we reached the target node, stop processing.
            if (currentNode == endNode) break

            // Iterate over all edges in the graph to find neighbors of the current node.
            for (edge in edges) {
                // Each edge connects two nodes, so consider both directions.
                val neighbors =
                    listOf(
                        edge.start to edge.end,
                        edge.end to edge.start,
                    )

                // Check each direction to find valid neighbors of the current node.
                for ((from, to) in neighbors) {
                    // If the current edge starts from this node and the neighbor isn't visited yet:
                    if (from == currentNode && !visited.contains(to)) {
                        // Skip edges marked as inaccessible if accessibility mode is enabled.
                        if (accessibilityMode && edge.accessible.equals("FALSE", ignoreCase = true)) {
                            continue
                        }

                        // Calculate the tentative distance to the neighbor through this edge.
                        val newDistance = currentDistance + edge.weight

                        // Update the shortest distance and the previous node if the new path is shorter.
                        if (newDistance < distances.getValue(to)) {
                            distances[to] = newDistance
                            previousNodes[to] = edge
                            // Add the neighbor to the priority queue with its updated distance.
                            priorityQueue.add(Pair(to, newDistance))
                        }
                    }
                }
            }
        }

        // Reconstruct the shortest path from the `previousNodes` map.
        val route = Route()
        var currentNode: Node? = endNode

        while (currentNode != null && previousNodes[currentNode] != null) {
            // Retrieve the edge that led to the current node.
            val edge = previousNodes[currentNode]
            if (edge != null) {
                // Add the edge to the route and backtrack to the previous node.
                route.addEdge(edge)
                currentNode = if (edge.start == currentNode) edge.end else edge.start
            }
        }

        // Reverse the order of edges to match the path from start to end.
        val reversedEdges = route.getEdges().toMutableList()
        reversedEdges.reverse()
        route.setEdges(reversedEdges)

        // Return the reconstructed route.
        return route
    }

    fun startRoute(
        destination: Node,
        context: Context,
        container: FrameLayout,
        mapImage: ImageView,
    ) {
        val userLocNode = Node(Pair(userCurrPosition.first, userCurrPosition.second), "Current")
        Log.d("Start Route", "User location is $userLocNode")
        val startNode = getClosestNode(userLocNode.position)

        if (startNode != null) {
            val dName = destination.name
            val sName = startNode.name
            Log.d("Start Route", "Starting route from $sName to $dName")
            // Hide the currently displayed route if it exists
            currentRoute?.hideRoute(container)

            // Calculate and display the new route
            val route = shortestPath(startNode, destination)
            val routeSize = route.getEdges().size
            Log.d("Start Route", "Route size is $routeSize")
            route.displayRoute(context, container, mapImage)

            // Save the new route as the currently displayed one
            currentRoute = route
        }
    }

    fun endCurrentRoute(container: FrameLayout) {
        currentRoute?.hideRoute(container)
        currentRoute = null // Clear the reference after hiding
    }
}

// should hold all nodes, landmarks and others
open class Node(
    var position: Pair<Double, Double>,
    val name: String,
)

// only landmarks
open class LandmarkNode(
    position: Pair<Double, Double>,
    name: String,
) : Node(position, name)

open class Edge(
    val start: Node,
    val end: Node,
    val accessible: String,
) {
    val weight: Double = calculateDistance(start.position, end.position)
    private var sudoNode: Node = start
    private var edgeView: EdgeView? = null

    fun display(
        context: Context,
        container: FrameLayout,
        mapImage: ImageView,
    ) {
        if (edgeView == null) {
            edgeView =
                EdgeView(context).apply {
                    init(this@Edge, mapImage)
                }
            container.addView(edgeView)
        }
    }

    fun hide(container: FrameLayout) {
        Log.d("EdgeView", "Hiding edge")
        edgeView?.let { view ->
            container.removeView(view)
            edgeView = null // Clear reference to allow garbage collection
        }
    }

    fun updateSudoNode(newPosition: Pair<Double, Double>) {
        sudoNode.position = newPosition
    }

    fun getSudoNode(): Node = sudoNode

    override fun toString(): String = "${start.name} to ${end.name} (Weight: $weight)"
}

// Route class representing the path between the user's location and the destination
// Responsible for calculating, displaying, and hiding the route on the map
open class Route {
    private val edges: MutableList<Edge> = mutableListOf()

    fun addEdge(edge: Edge) {
        edges.add(edge)
    }

    fun getEdges(): List<Edge> = edges

    fun getTotalWeight(): Double = edges.sumOf { it.weight }

    fun setEdges(newEdges: List<Edge>) {
        edges.clear()
        edges.addAll(newEdges)
    }

    fun printRoute() {
        for (edge in edges) {
            println("${edge.start.name} to ${edge.end.name} (Weight: ${edge.weight})")
        }
    }

    fun displayRoute(
        context: Context,
        container: FrameLayout,
        mapImage: ImageView,
    ) {
        Log.d("Route", "Displaying route")
        Log.d("Route", "Edge size: ${edges.size}")
        for (edge in edges) {
            edge.display(context, container, mapImage)
        }
    }

    fun hideRoute(container: FrameLayout) {
        for (edge in edges) {
            edge.hide(container)
        }
    }

    fun calculateDistance(): Double {
        var distance = 0.0
        for (edge in edges) {
            distance += edge.weight
        }
        return distance
    }

    override fun toString(): String = edges.joinToString(separator = " -> ") { "${it.start.name} to ${it.end.name} (Weight: ${it.weight})" }
}

// Pin class to represent a user placed location on the map
open class Pin(
    position: Pair<Double, Double>,
    name: String,
) : Node(position, name)
