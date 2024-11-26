@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.myapplication

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GraphTest {
    private lateinit var graph: Graph

    @BeforeEach
    fun setUp() {
        graph = Graph()

        val landmarks =
            listOf(
                LandmarkNode(Pair(10.0, 20.0), "Landmark A"),
                LandmarkNode(Pair(15.0, 25.0), "Landmark B"),
            )

        val nodes =
            listOf(
                Node(Pair(11.0, 21.0), "Node A"),
                Node(Pair(16.0, 26.0), "Node B"),
                Node(Pair(16.0, 26.0), "Node C"),
            )

        for (landmark in landmarks) {
            graph.addNode(landmark)
        }

        for (node in nodes) {
            graph.addNode(node)
        }

        // Add edges (assuming these nodes exist)
        graph.addEdge(
            Edge(
                start = graph.getNodeByName("Landmark A")!!,
                end = graph.getNodeByName("Node A")!!,
                accessible = "TRUE",
            ),
        )
        graph.addEdge(
            Edge(
                start = graph.getNodeByName("Landmark B")!!,
                end = graph.getNodeByName("Node B")!!,
                accessible = "FALSE",
            ),
        )
        graph.addEdge(
            Edge(
                start = graph.getNodeByName("Landmark B")!!,
                end = graph.getNodeByName("Node C")!!,
                accessible = "TRUE",
            ),
        )
        graph.addEdge(
            Edge(
                start = graph.getNodeByName("Node A")!!,
                end = graph.getNodeByName("Node B")!!,
                accessible = "FALSE",
            ),
        )
        graph.addEdge(
            Edge(
                start = graph.getNodeByName("Node A")!!,
                end = graph.getNodeByName("Node C")!!,
                accessible = "TRUE",
            ),
        )
    }

    @Test
    fun testAddNode() {
        val newNode = Node(Pair(5.0, 6.0), "Node C")
        graph.addNode(newNode)
//        for (node in graph.nodes) {
//            println("Name: ${node.name}, Position: (${node.position.first}, ${node.position.second})")
//        }
        assertEquals(6, graph.nodes.size)
        assertEquals("Node C", graph.nodes[4].name)
    }

    @Test
    fun testGetAllLandmarkNodeNames() {
        val landmarkNames = graph.getAllLandmarkNodeNames()
        assertArrayEquals(arrayOf("Landmark A", "Landmark B"), landmarkNames)
    }

    @Test
    fun testGetNodeByName() {
        val node = graph.getNodeByName("Node A")
        assertNotNull(node)
        assertEquals("Node A", node!!.name)
    }

    @Test
    fun testGetClosestNode() {
        val closestNode = graph.getClosestNode(Pair(10.5, 20.5))
        assertNotNull(closestNode)
        assertEquals("Node A", closestNode!!.name)
    }

    @Test
    fun testShortestPathAccessFalse() {
        val currAccess = accessibilityMode
        accessibilityMode = false
        val startNode = graph.getNodeByName("Landmark A")!!
        val endNode = graph.getNodeByName("Landmark B")!!
        val route = graph.shortestPath(startNode, endNode)
        route.printRoute()
        accessibilityMode = currAccess
        assertNotNull(route)
        assertEquals(3, route.getEdges().size) // Assuming 1 edge exists between these two nodes
        assertEquals("Landmark A", route.getEdges()[0].start.name)
        assertEquals("Node B", route.getEdges()[2].end.name)
    }

    @Test
    fun testShortestPathAccessTrue() {
        val currAccess = accessibilityMode
        accessibilityMode = true
        val startNode = graph.getNodeByName("Landmark A")!!
        val endNode = graph.getNodeByName("Landmark B")!!
        val route = graph.shortestPath(startNode, endNode)
        route.printRoute()
        accessibilityMode = currAccess
        assertNotNull(route)
        assertEquals(3, route.getEdges().size) // Assuming 1 edge exists between these two nodes
        assertEquals("Landmark A", route.getEdges()[0].start.name)
        assertEquals("Node C", route.getEdges()[2].end.name)
    }

    // fill in later if necessary
    @Test
    fun testParseLandmarksFromCSV() {
    }

    @Test
    fun testParseNodesFromCSV() {
    }

    @Test
    fun testParseEdgesFromCSV() {
    }
}
