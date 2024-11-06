package com.example.myapplication
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class GraphTest {

    private lateinit var graph: Graph

    @BeforeEach
    fun setUp() {
        graph = Graph()

        // Mock loading of CSV files for testing
        // You would replace these with actual test data as necessary
        val landmarks = listOf(
            LandmarkNode(Pair(10.0, 20.0), "Landmark A"),
            LandmarkNode(Pair(15.0, 25.0), "Landmark B")
        )

        val nodes = listOf(
            Node(Pair(1.0, 2.0), "Node A"),
            Node(Pair(3.0, 4.0), "Node B")
        )

        for (landmark in landmarks) {
            graph.AddNode(landmark)
        }

        for (node in nodes) {
            graph.AddNode(node)
        }

        // Add edges (assuming these nodes exist)
        graph.AddEdge(Edge(start = graph.GetNodeByName("Landmark A")!!, end = graph.GetNodeByName("Node A")!!))
        graph.AddEdge(Edge(start = graph.GetNodeByName("Landmark B")!!, end = graph.GetNodeByName("Node B")!!))
    }

    @Test
    fun testAddNode() {
        val newNode = Node(Pair(5.0, 6.0), "Node C")
        graph.AddNode(newNode)
        for (node in graph.nodes) {
            println("Name: ${node.name}, Position: (${node.position.first}, ${node.position.second})")
        }
        assertEquals(5, graph.nodes.size)
        assertEquals("Node C", graph.nodes[4].name)
    }

    @Test
    fun testGetAllLandmarkNodeNames() {
        val landmarkNames = graph.GetAllLandmarkNodeNames()
        assertArrayEquals(arrayOf("Landmark A", "Landmark B"), landmarkNames)
    }

    @Test
    fun testGetNodeByName() {
        val node = graph.GetNodeByName("Node A")
        assertNotNull(node)
        assertEquals("Node A", node!!.name)
    }

    @Test
    fun testGetClosestNode() {
        val closestNode = graph.GetClosestNode(Pair(10.5, 20.5))
        assertNotNull(closestNode)
        assertEquals("Landmark A", closestNode!!.name)
    }

    @Test
    fun testShortestPath() {
        val startNode = graph.GetNodeByName("Landmark A")!!
        val endNode = graph.GetNodeByName("Node A")!!
        val route = graph.ShortestPath(startNode, endNode)

        assertNotNull(route)
        assertEquals(1, route.getEdges().size) // Assuming 1 edge exists between these two nodes
        assertEquals("Landmark A", route.getEdges()[0].start.name)
        assertEquals("Node A", route.getEdges()[0].end.name)
    }

    @Test
    fun testParseLandmarksFromCSV() {
        // You can implement mock behavior to simulate reading from CSV
    }

    @Test
    fun testParseNodesFromCSV() {
        // You can implement mock behavior to simulate reading from CSV
    }

    @Test
    fun testParseEdgesFromCSV() {
        // You can implement mock behavior to simulate reading from CSV
    }
}
