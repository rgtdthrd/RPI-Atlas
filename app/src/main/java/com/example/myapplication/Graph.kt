package com.example.myapplication

open class Node(val position: Pair<Double, Double>) {

}

open class SearchableNode(position: Pair<Double, Double>, val image_position: Pair<Int, Int>, val name: String) : Node(position) {

}