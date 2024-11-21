package com.example.myapplication

import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import java.io.BufferedReader
import java.io.InputStreamReader

class Graph {
    var nodes = emptyArray<Node>()
    var edges = emptyArray<Edge>()
    private var currentRoute: Route? = null

    fun addNode(newNode: Node) {
        nodes += newNode
    }

    fun addEdge(newEdge: Edge) {
        edges += newEdge
    }

    fun getAllEdges(): Array<Edge> = edges

    fun getAllLandmarkNodeNames(): Array<String> {
        var outArray = emptyArray<String>()
        for (node in nodes) {
            if (node is LandmarkNode) {
                outArray += node.name
            }
        }
        return outArray
    }

    fun getNodeByName(name: String): Node? {
        for (node in nodes) {
            if (node.name.equals(name, ignoreCase = true)) {
                return node
            }
        }
        return null
    }

    fun getLandmarkNodeByName(name: String): LandmarkNode? {
        for (node in nodes) {
            if (node is LandmarkNode && node.name.equals(name, ignoreCase = true)) {
                return node
            }
        }
        return null
    }

    fun parseLandmarksFromCSV(
        context: Context,
        resourceId: Int,
    ): List<LandmarkNode> {
        val nodeList = mutableListOf<LandmarkNode>()
        val inputStream = context.resources.openRawResource(resourceId)
        val reader = BufferedReader(InputStreamReader(inputStream))

        try {
            var line: String?
            reader.readLine() // skip the title line
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

    // Edge class is not implemented yet, but this should work when it is implemented and the two lines are uncommented.
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
                    assert(columns.size == 2)
                    val nodeName1 = columns[0]
                    val nodeName2 = columns[1]
                    val start = getNodeByName(nodeName1)
                    val end = getNodeByName(nodeName2)
                    if (start != null && end != null) {
                        val edge = Edge(start = start, end = end)
                        addEdge(edge)
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

    fun getClosestNode(position: Pair<Double, Double>): Node? {
        if (nodes.isEmpty()) return null // Return null if there are no nodes in the graph
        var closestNode: Node? = null
        var minDistance = Double.POSITIVE_INFINITY
        // check all nodes for distance, then return the closest one
        for (node in nodes) {
            val distance = calculateDistance(position, node.position)
            if (distance < minDistance) {
                minDistance = distance
                closestNode = node
            }
        }
        return closestNode
    }

    // Dijkstra's to find shortest path between 2 nodes
    fun shortestPath(
        startNode: Node,
        endNode: Node,
    ): Route {
        // Maps each node to the shortest distance from the start node, defaulting to infinity
        val distances = mutableMapOf<Node, Double>().withDefault { Double.POSITIVE_INFINITY }

        // Keeps track of the previous edge leading to each node for path reconstruction
        val previousNodes = mutableMapOf<Node, Edge?>()

        // Tracks nodes that have already been visited
        val visited = mutableSetOf<Node>()

        // Priority queue for selecting the node with the shortest known distance
        val priorityQueue = java.util.PriorityQueue(compareBy<Pair<Node, Double>> { it.second })

        // Initialize start node's distance to 0 and add it to the queue
        distances[startNode] = 0.0
        priorityQueue.add(Pair(startNode, 0.0))

        // Main loop: process nodes in order of distance from the start node
        while (priorityQueue.isNotEmpty()) {
            // Get the node with the smallest distance in the queue
            val (currentNode, currentDistance) = priorityQueue.poll()!!

            // Skip this node if it’s already been visited
            if (visited.contains(currentNode)) continue
            visited.add(currentNode)

            // Stop if we've reached the end node
            if (currentNode == endNode) break

            // Iterate over all edges to find neighbors, treating each edge as bidirectional
            for (edge in edges) {
                // Create pairs for both directions of the edge: (start -> end) and (end -> start)
                val neighbors =
                    listOf(
                        edge.start to edge.end,
                        edge.end to edge.start,
                    )

                // Check each direction (from -> to) to find unvisited neighbors
                for ((from, to) in neighbors) {
                    // If the current node is the start of this edge and the end is unvisited
                    if (from == currentNode && !visited.contains(to)) {
                        // Calculate the new distance to this neighbor
                        val newDistance = currentDistance + edge.weight

                        // If this path to 'to' is shorter, update distances and previousNodes
                        if (newDistance < distances.getValue(to)) {
                            distances[to] =
                                newDistance // Update shortest distance to this node
                            previousNodes[to] = edge // Record the edge leading to this node
                            priorityQueue.add(
                                Pair(
                                    to,
                                    newDistance,
                                ),
                            ) // Add the neighbor to the queue
                        }
                    }
                }
            }
        }

        // Reconstruct the shortest path by backtracking from the end node
        val route = Route()
        var currentNode: Node? = endNode

        // Follow previous nodes from end node to start node, adding each edge to the route
        while (currentNode != null && previousNodes[currentNode] != null) {
            val edge = previousNodes[currentNode]
            if (edge != null) {
                route.addEdge(edge) // Add the edge to the route
                // Move to the previous node, based on the direction of the edge
                currentNode = if (edge.start == currentNode) edge.end else edge.start
            }
        }

        // Reverse the collected edges to get the path from start to end
        val reversedEdges = route.getEdges().toMutableList()
        reversedEdges.reverse()
        route.setEdges(reversedEdges)

        // Return the constructed route with the shortest path
        return route
    }

    fun startRoute(
        destination: Node,
        context: Context,
        container: FrameLayout,
        mapImage: ImageView,
    ): Boolean {
        val userLocNode = Node(Pair(userCurrPosition.first, userCurrPosition.second), "Current")
        val startNode = getClosestNode(userLocNode.position)
        var userFarFromDest = true

        if (startNode != null) {
            // Hide the currently displayed route if it exists
            currentRoute?.hideRoute(container)

            // Calculate and display the new route
            val route = shortestPath(startNode, destination)
            route.displayRoute(context, container, mapImage)

            // Save the new route as the currently displayed one
            currentRoute = route

            // Check proximity to the destination
            if (calculateDistance(
                    userLocNode.position,
                    destination.position,
                ) < Companion.CLOSE_THRESHOLD
            ) {
                userFarFromDest = false
            }
        }

        return userFarFromDest
    }

    fun endCurrentRoute(container: FrameLayout) {
        currentRoute?.hideRoute(container)
        currentRoute = null // Clear the reference after hiding
    }

    companion object {
        const val CLOSE_THRESHOLD = 0.02
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

    fun update(
        userLoc: Pair<Double, Double>,
        container: FrameLayout,
    ) {
        edgeView?.update(userLoc, container)
    }

    fun updateSudoNode(newPosition: Pair<Double, Double>) {
        sudoNode.position = newPosition
    }

    fun getSudoNode(): Node = sudoNode

    override fun toString(): String = "${start.name} to ${end.name} (Weight: $weight)"
}

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
