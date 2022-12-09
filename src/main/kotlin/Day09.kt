import kotlin.math.absoluteValue

fun main() {
    val input = readResourceLines("Day9.txt")
    val output = findVisitedLocations(input)
    println("$output")
}

fun findVisitedLocations(input: List<String>): Int {
    val rope = Rope()
    input.forEach { line ->
        val (direction, amount) = line.split(" ")

        val move = when (direction) {
            "L" ->  Move.Left
            "R" ->  Move.Right
            "U" -> Move.Up
            "D" -> Move.Down
            else -> error("Can't parse $direction to any Move")
        }

        rope.move(move, amount.toInt())
    }

    return rope.visitedLocations.size
}

class Rope() {
    private var tailPosition: Pair<Int, Int> = Pair(0, 0)
    private var headPosition: Pair<Int, Int> = Pair(0, 0)
    val visitedLocations: MutableSet<Pair<Int, Int>> = mutableSetOf()

    private fun isCovered() = tailPosition == headPosition

    private fun isAdjacent(): Boolean {
        val delta = headPosition - tailPosition
        return !(delta.first.absoluteValue > 1 || delta.second.absoluteValue > 1)
    }

    fun move(move: Move, amount: Int) {
        for (i in 1..amount) {
            moveOne(move)
            visitedLocations.add(tailPosition)
//            printGrid()
        }
    }

    @Suppress("unused")
    private fun printGrid() {
        val grid = List(5) { MutableList(6) { "." } }
        grid[4][0] = "S"
        grid[4 - tailPosition.second][ tailPosition.first] = "T"
        grid[4 - headPosition.second][ headPosition.first] = "H"

        grid.forEach { line ->
            line.forEach { character ->
                print(character)
            }
            print("\n")
        }
        print("\n")
    }

    private fun moveOne(move: Move) {
        if(isCovered()) {
            headPosition += move.position
            return
        }

        if(headPosition - move.position == tailPosition) {
            headPosition += move.position
            tailPosition += move.position
            return
        }

        headPosition += move.position
        if(!isAdjacent()) {
            val delta = headPosition - tailPosition
            tailPosition += if(delta.first > 0) Move.Right.position else Move.Left.position
            tailPosition += if(delta.second > 0) Move.Up.position else Move.Down.position
        }
    }
}

enum class Move(val position: Pair<Int, Int>) {
    Left(Pair(-1, 0)),
    Right(Pair(1, 0)),
    Up(Pair(0, 1)),
    Down(Pair(0, -1)),
}

operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>) = Pair(this.first + other.first, this.second + other.second)
operator fun Pair<Int, Int>.minus(other: Pair<Int, Int>) = Pair(this.first - other.first, this.second - other.second)
