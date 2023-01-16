import java.util.*
import kotlin.Comparator
import kotlin.math.absoluteValue

fun main() {
    val input = readResourceLines("Day24Test.txt")
//    val output = findShortestPath(input)
    val output = findShortestPathToRetrieveSnack(input)
    println("The shortest path takes: $output minutes")
}

private fun parseBlizzardMaze(input: List<String>): BlizzardMaze {
    val windParsingMap = WindDirection.createParsingMap()
    val height = input.size - 2
    val width = input.first().length - 2

    val winds = input.flatMapIndexed { rowIndex, line ->
        line.mapIndexedNotNull { columnIndex, char ->
            if(char == '#' || char == '.') return@mapIndexedNotNull null
            Wind(windParsingMap[char]!!, rowIndex -1 to columnIndex -1)
        }
    }

    return BlizzardMaze(winds, height, width, height to width - 1)
}

val mazeStart = -1 to 0
fun findShortestPath(input: List<String>): Int {
    val maze = parseBlizzardMaze(input)
    return maze.search(BlizzardMaze.Position(mazeStart, 0, 0), maze.mazeGoal)
}

fun findShortestPathToRetrieveSnack(input: List<String>): Int {
    val maze = parseBlizzardMaze(input)

    val startPosition = BlizzardMaze.Position(mazeStart, 0, 0)
    val firstPath = maze.search(startPosition, maze.mazeGoal)

    val endPosition = BlizzardMaze.Position(maze.mazeGoal, firstPath, 0)
    val secondPath = maze.search(endPosition, mazeStart)

    val secondStartPosition = BlizzardMaze.Position(mazeStart, secondPath, 0)
    val thirdPath = maze.search(secondStartPosition, maze.mazeGoal)

    return firstPath + secondPath + thirdPath
}

typealias Layout = Set<Pair<Int, Int>>
private class BlizzardMaze(
    blizzards: List<Wind>,
    val height: Int,
    val width: Int,
    val mazeGoal: Pair<Int, Int>,
) {
    private val layouts: Map<Int, Layout>
    private fun getCurrentLayout(time: Int) = layouts[time % layouts.size]!!

    init {
        var currentBlizzards = blizzards
        layouts = (0 until leastCommonMultiple(height, width)).associateWith { _ ->
            val result = currentBlizzards
            currentBlizzards = currentBlizzards.evolve()
            result.map { it.position }.toSet()
        }
    }

    private fun List<Wind>.evolve(): List<Wind> {
        return this.map { wind ->
            val (newRow, newColumn) = wind.position + wind.direction.direction
            val finalRow = newRow.mod(height)
            val finalColumn = newColumn.mod(width)
            Wind(wind.direction, finalRow to finalColumn)
        }
    }

    private val reachedPositions = mutableSetOf<Position>()
    private val priorityQueue = PriorityQueue(PositionComparator())
    fun search(startPosition: Position, goal: Pair<Int, Int>): Int {
        priorityQueue.add(startPosition)

        while(priorityQueue.isNotEmpty()) {
            val positionToExpand = priorityQueue.remove()
            val nextTime = positionToExpand.time + 1
            val nextLayout = getCurrentLayout(nextTime)


            val newPositions = Choices.values().mapNotNull { choice ->
                val nextLocation = positionToExpand.location + choice.direction
                if(nextLocation == goal) return nextTime
                if(nextLocation != startPosition.location) {
                    if(nextLocation.first < 0 || nextLocation.second < 0) return@mapNotNull null
                    if(nextLocation.second < 0) return@mapNotNull null
                }

                if(nextLocation.first >= height || nextLocation.second >= width) return@mapNotNull null
                if(nextLayout.contains(nextLocation)) return@mapNotNull null

                val delta = mazeGoal - nextLocation
                val priority = delta.first.absoluteValue + delta.second.absoluteValue + nextTime

//                println("minute $nextTime")
//                nextLayout.print(nextLocation)
                val nextPosition = Position(nextLocation, nextTime, priority)
                if(reachedPositions.contains(nextPosition)) return@mapNotNull null

                nextPosition
            }

            reachedPositions.add(positionToExpand)
            reachedPositions.addAll(newPositions)
            priorityQueue.addAll(newPositions)
        }

        return error("no path found")
    }

    fun Layout.print(playerPosition: Pair<Int, Int>) {
        val board = List(height + 2) { CharArray(width + 2) { '.' } }
        for (row in 0 until height + 2 ) {
            board[row][0] = '#'
            board[row][width + 1] = '#'
        }

        for (column in 0 until width + 2) {
            board[0][column] = '#'
            board[height + 1][column] = '#'
        }

        this.map { board[it.first + 1][it.second + 1] = '<'}
        board[playerPosition.first + 1][playerPosition.second + 1] = 'E'

        board.forEach { println(it) }
        println()
    }

    data class Position(val location: Pair<Int, Int>, val time: Int, val priority: Int)

    private class PositionComparator : Comparator<Position> {
        override fun compare(first: Position, second: Position) = first.priority.compareTo(second.priority) //compareTo
    }

    private enum class Choices(val direction: Pair<Int, Int>) {
        Up(-1 to 0),
        Down(1 to 0),
        Left(0 to -1),
        Right(0 to 1),
        Stay(0 to 0)
    }
}

private fun leastCommonMultiple(a: Int, b: Int) = (a * b) / gcd(a, b)

private fun gcd(a: Int, b: Int): Int {
    val (smaller, bigger) = listOf(a, b).sorted()
    val newBigger = bigger - smaller
    if(newBigger == smaller) return newBigger

    return gcd(newBigger, smaller)
}


private data class Wind(val direction: WindDirection, val position: Pair<Int, Int>)
private enum class WindDirection(val char: Char, val direction: Pair<Int, Int>) {
    Up('^', -1 to 0),
    Down('v', 1 to 0),
    Left('<', 0 to -1),
    Right('>', 0 to 1);

    companion object {
        fun createParsingMap() = values().associateBy { it.char }
    }
}

private operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>) = this.first + other.first to this.second + other.second
private operator fun Pair<Int, Int>.minus(other: Pair<Int, Int>) = this.first - other.first to this.second - other.second
