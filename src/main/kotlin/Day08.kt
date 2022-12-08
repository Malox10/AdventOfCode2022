fun main() {
    val input = readResourceLines("Day8.txt")
//    val output = findVisibleTrees(input)
    val output = mostVisibleTree(input)
    println("$output")
}

fun mostVisibleTree(input: List<String>): Int {
    val heightMap = input.map { row -> row.map { it.digitToInt() } }
    val height = input.size
    val width = input.first().length

    return heightMap.flatMapIndexed { rowIndex, trees ->
        trees.mapIndexed { columnIndex, tree ->
            val directions = listOf(
                Pair(rowIndex..rowIndex, (0 until columnIndex).reversed()), //west
                Pair(rowIndex..rowIndex, columnIndex + 1 until width), //east
                Pair((0 until rowIndex).reversed(), columnIndex..columnIndex), //north
                Pair(rowIndex + 1 until height, columnIndex..columnIndex), //south
            )

            val lookingDirections = directions.map { (rowIndices, columnIndices) ->
                var numberOfVisibleTrees = 0
                for (row in rowIndices) {
                    for (column in columnIndices) {
                        numberOfVisibleTrees++
                        if(heightMap[row][column] >= tree) return@map numberOfVisibleTrees
                    }
                }
                numberOfVisibleTrees
            }
            lookingDirections.reduce { a,b -> a * b}
        }
    }.max()
}

@Suppress("unused")
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

