package com.example.myapplication

const val NUMRESULTS = 5

private fun editDistance(s: String, t: String): Int {
    val m = s.length
    val n = t.length
    val d = Array(m + 1) {Array<Int>(n + 1) {0}}
    for (i in 1..m) {
        d[i][0] = i
    }
    for (j in 1..n) {
        d[0][j] = j
    }
    var cost: Int
    for (j in 1..n) {
        for (i in 1..m) {
            if (s[i - 1] == t[j - 1]) {
                cost = 0
            } else {
                cost = 1
            }
            d[i][j] = minOf(
                d[i - 1][j] + 1,
                d[i][j - 1] + 1,
                d[i - 1][j - 1] + cost
            )
        }
    }
    return d[m][n]
}

private fun matchVal(s: String, t: String): Int {
    return editDistance(s.uppercase(), t.uppercase())
}


fun FuzzySearch(searchword: String, all_terms: Array<String>): Array<String> {
    val scores = mutableMapOf<String, Int>()
    for (term in all_terms) {
        scores[term] = matchVal(searchword, term)
    }
    val newmap = scores.toSortedMap(compareBy<String> {scores[it]}.thenBy{it})
    val results = Array<String>(NUMRESULTS) {" "}
    var i = 0
    for (result in newmap.keys) {
        if (i < NUMRESULTS) {
            results[i] = result
            i++
        }
    }
    return results
}