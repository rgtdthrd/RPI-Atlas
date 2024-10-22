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
}

open class Node(val position: Pair<Double, Double>) {

}

open class SearchableNode(position: Pair<Double, Double>, val name: String) : Node(position) {

}