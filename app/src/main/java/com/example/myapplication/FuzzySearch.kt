package com.example.myapplication

const val RESULTS = 5

// calculate the edit distance between two strings
private fun editDistance(
    leftIndex: String,
    rightIndex: String,
): Int {
    val m = leftIndex.length
    val n = rightIndex.length
    // dynamic programming table
    val d = Array(m + 1) { Array(n + 1) { 0 } }

    // initialize the dynamic programming table
    for (i in 1..m) {
        d[i][0] = i
    }
    for (j in 1..n) {
        d[0][j] = j
    }

    // fill in the dynamic programming table
    var cost: Int
    for (j in 1..n) {
        for (i in 1..m) {
            cost =
                if (leftIndex[i - 1] == rightIndex[j - 1]) {
                    0
                } else {
                    1
                }
            d[i][j] =
                minOf(
                    d[i - 1][j] + 1,
                    d[i][j - 1] + 1,
                    d[i - 1][j - 1] + cost,
                )
        }
    }
    return d[m][n]
}

// calculate the match value between two strings
private fun matchVal(
    s: String,
    t: String,
): Int = editDistance(s.uppercase(), t.uppercase())

// fuzzy search for SearchableNode names
fun fuzzySearch(
    searchWord: String,
    allTerms: Array<String>,
): Array<String> {
    // calculate the editDistance "scores" of all terms to the search word
    val scores = mutableMapOf<String, Int>()
    for (term in allTerms) {
        scores[term] =
            matchVal(searchWord, term.substring(0, minOf(searchWord.length, term.length)))
    }
    // sort the terms by their scores
    val newMap = scores.toSortedMap(compareBy<String> { scores[it] }.thenBy { it })
    val results = Array(RESULTS) { " " }
    var i = 0
    for (result in newMap.keys) {
        if (i < RESULTS) {
            results[i] = result
            i++
        }
    }
    return results
}
