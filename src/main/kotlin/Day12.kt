import java.util.*
import kotlin.math.absoluteValue

fun main() {
    val input = readResourceLines("Day12.txt")
    val output = findPathLength(input)
    println("The shortest path is length: $output")
}

fun findPathLength(input: List<String>): Int {
    var start: Pair<Int, Int> = 0 to 0
    var end: Pair<Int, Int> = 0 to 0

    val grid = input.mapIndexed { rowIndex, line ->
        line.mapIndexed { columnIndex, char ->
            val height = when (char) {
                'S' -> {
                    start = rowIndex to columnIndex
                    1
                }
                'E' -> {
                    end = rowIndex to columnIndex
                    26
                }
                else -> char.toHeight()
            }

            height
        }
    }

    val path = aStar(grid, AStarNode(start, null, 0, start.distanceBetween(end)), AStarNode(end, null, 0))
    printPath(path, input)
    return path.currentPathLength
}

fun printPath(node: AStarNode, input: List<String>) {
    val grid = input.map { line -> line.map { it }.toMutableList() }

    var currentNode = node
    while(currentNode.previous != null) {
        val (row, column) = currentNode.location
//        val char = grid[row][column]
//        if(char.isUpperCase()) continue

        val newChar = grid[row][column].uppercaseChar()
        grid[row][column] = newChar
        currentNode = currentNode.previous!!
    }

    grid.forEach { line ->
        line.forEach { char ->
            print(char)
        }
        print("\n")
    }
    print("\n")
}


data class AStarNode(
    val location: Pair<Int, Int>,
    val previous: AStarNode?,
    val currentPathLength: Int,
    val fValue: Int = 0,
)

val openList = PriorityQueue(AStarNodeComparator())
val closedList = mutableSetOf<AStarNode>()

private fun aStar(grid: List<List<Int>>, start: AStarNode, end: AStarNode): AStarNode {
    openList.add(start)

    do {
        val currentNode = openList.remove()
        if(currentNode.location == end.location) return currentNode

        closedList.add(currentNode)
        expandNode(currentNode, grid, end)
    } while(!openList.isEmpty())
    error("No Path Found")
}

fun expandNode(node: AStarNode, grid: List<List<Int>>, end: AStarNode) {
    val neighbours = node.getNeighbours(grid, end.location)

    neighbours.forEach { neighbour ->
        if(closedList.find { it.location == neighbour.location } != null) return@forEach

        val nodeInOpenList = openList.find { it.location == neighbour.location }
        if(nodeInOpenList != null) {
            if(nodeInOpenList.currentPathLength <= neighbour.currentPathLength) return@forEach
        }

        if(nodeInOpenList != null) openList.remove(nodeInOpenList)
        openList.add(neighbour)
    }
}

fun AStarNode.getNeighbours(grid: List<List<Int>>, end: Pair<Int, Int>): List<AStarNode> {
    val potentialNeighbours = listOf(
        1 to 0,
        0 to 1,
        -1 to 0,
        0 to -1,
    ).map { it + this.location }

    val possibleHeight = this.location.getHeight(grid) + 1

    val neighbours = potentialNeighbours.filter { potentialNeighbour ->
        if(potentialNeighbour == this.previous?.location) return@filter false
        if(!grid.isInBounds(potentialNeighbour)) return@filter false

        potentialNeighbour.getHeight(grid) <= possibleHeight
    }

    return neighbours.map { location ->
        val g = this.currentPathLength + 1
        val f = g + location.distanceBetween(end)
        AStarNode(location, this, g, f)
    }
}

fun<T> List<List<T>>.isInBounds(index: Pair<Int, Int>): Boolean {
    if(this.isEmpty()) return false
    if(index.first < 0 || index.second < 0) return false

    val rowAmount = this.size
    val columnAmount = this.first().size

    if(index.first >= rowAmount || index.second >= columnAmount) return false
    return true
}

internal class AStarNodeComparator : Comparator<AStarNode> {
    override fun compare(first: AStarNode, second: AStarNode) = first.fValue.compareTo(second.fValue)
}

fun Pair<Int,Int>.getHeight(grid: List<List<Int>>) = grid[this.first][this.second]
private operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>) = this.first + other.first to this.second + other.second
private operator fun Pair<Int, Int>.minus(other: Pair<Int, Int>) = Pair(this.first - other.first, this.second - other.second)
private fun Pair<Int, Int>.distanceBetween(other: Pair<Int, Int>): Int {
    val delta = this - other
    return delta.first.absoluteValue + delta.second.absoluteValue
}
fun Char.toHeight() = this.code - 96