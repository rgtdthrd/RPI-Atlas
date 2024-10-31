package com.example.myapplication

class Graph() {
    var nodes = emptyArray<Node>()

    fun AddNode(new_node: Node) {
        nodes += new_node
    }

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


}

open class Node(val position: Pair<Double, Double>, val name: String) {

}

open class SearchableNode(position: Pair<Double, Double>, name: String) : Node(position, name) {

}