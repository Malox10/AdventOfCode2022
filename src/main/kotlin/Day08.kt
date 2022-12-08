fun main() {
    val input = readResourceLines("Day8.txt")
    val output = findVisibleTrees(input)
    println("$output")
}

fun findVisibleTrees(input: List<String>): Int {
    val height = input.size
    val width = input.first().length

    val mask = Array(height) { BooleanArray(width) { false } }

    val directionIndices = listOf(
        Pair(Pair(0 until height, 0 until width), Direction.Horizontal), //west to east
        Pair(Pair(0 until height, (0 until width).reversed()), Direction.Horizontal), //east to west
        Pair(Pair(0 until width, 0 until height), Direction.Vertical), //north to south
        Pair(Pair(0 until width, (0 until height).reversed()), Direction.Vertical) //south to north
    )

    directionIndices.forEach { (indices, direction) ->
        val (rowIndices, columnIndices) = indices
        for (row in rowIndices) {
            var highestTree = -1
            for (column in columnIndices) {
                val char = if (direction == Direction.Horizontal) input[row][column] else input[column][row]
                val treeHeight = char.digitToInt() //map beforehand to avoid unnecessary calculations

                if(treeHeight <= highestTree) continue
                highestTree = treeHeight
                if (direction == Direction.Horizontal) mask[row][column] = true
                else mask[column][row] = true

                if(treeHeight >= 9) break
            }
        }
    }

    return mask.sumOf { row -> row.count { it } }
}

enum class Direction {
    Horizontal,
    Vertical
}

