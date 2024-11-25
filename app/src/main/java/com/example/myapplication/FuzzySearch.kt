package com.example.myapplication

const val RESULTS = 5

private fun editDistance(
    s: String,
    t: String,
): Int {
    val m = s.length
    val n = t.length
    val d = Array(m + 1) { Array(n + 1) { 0 } }
    for (i in 1..m) {
        d[i][0] = i
    }
    for (j in 1..n) {
        d[0][j] = j
    }
    var cost: Int
    for (j in 1..n) {
        for (i in 1..m) {
            cost = if (s[i - 1] == t[j - 1]) {
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

private fun matchVal(
    s: String,
    t: String,
): Int = editDistance(s.uppercase(), t.uppercase())

fun fuzzySearch(
    searchWord: String,
    allTerms: Array<String>,
): Array<String> {
    val scores = mutableMapOf<String, Int>()
    for (term in allTerms) {
        scores[term] =
            matchVal(searchWord, term.substring(0, minOf(searchWord.length, term.length)))
    }
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
