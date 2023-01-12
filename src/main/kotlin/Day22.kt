@file:Suppress("unused")
import java.util.*

fun main() {
    val input = readResource("Day22.txt")
//    val output = findPassword(input)
    val output = findCubePassword(input)
    println("The password is: $output")
}

private fun parseMaze(input: String): Maze {
    val (maze, moves) = input.split("\r\n\r\n")

    val mazeLines = maze.lines().map { it.toCharArray() }
    val parts = moves
        .replace("L", ".L.")
        .replace("R", ".R.")
        .split(".")
    val mazeMoves = (listOf("L") + parts)
        .chunked(2)
        .map { (direction, amount) ->
            Turn(direction == "L", amount.toInt())
        }


    return Maze(mazeLines, mazeMoves, parts.first().toInt())
}

private fun findPassword(input: String): Int {
    val maze = parseMaze(input)
    return maze.moveThroughMaze()
}

private fun findCubePassword(input: String): Int {
    val maze = parseMaze(input)
    val edgeTraverser = EdgeTraverser(maze.field)
    val pairs = edgeTraverser.traverse()
    val map = pairs.flatMap { (a, b) ->
        listOf(a to b, b to a)
    }.associate { it }

    maze.edgeMap = map
    return maze.moveThroughMaze()
}

private typealias Edge = Pair<Location, Facing>
private class EdgeTraverser(
    val field: List<CharArray>,
) {
    val facing: LoopedFacing = LoopedFacing().also { it.setFacingTo(Facing.Down) }
    val facingOfEdge: LoopedFacing = LoopedFacing().also { it.setFacingTo(Facing.Left) }

    var currentLocation = Location(0, field.first().indexOfLast { it != ' ' } + 1)

    fun traverse(): List<Pair<Edge, Edge>> {
        val edgePairs = mutableListOf<Pair<Edge, Edge>>()
        val stack = Stack<Edge>()
        var isCollecting = true
        val firstElement = currentLocation + Facing.Left.step

        do {
            if(stack.isEmpty()) isCollecting = true
            val locationOnMaze = currentLocation + facingOfEdge.currentFacing().step

            val nextPosition = currentLocation + facing.currentFacing().step
            val nextSquare = nextPosition.getChar() ?: ' '
            if((locationOnMaze.getChar() ?: ' ') == ' ') {
                facing.turn(false)
                facingOfEdge.turn(false)
                currentLocation += facing.currentFacing().step
                continue
            }

            if(isCollecting) stack.push(locationOnMaze to facingOfEdge.currentFacing())
            else {
                val currentEdge: Edge = locationOnMaze to facingOfEdge.currentFacing()
                edgePairs.add(stack.pop() to currentEdge)
                if(locationOnMaze == firstElement) return edgePairs
            }

            if(nextSquare == ' ') {
                currentLocation = nextPosition
            } else {
                isCollecting = false
                facing.turn(true)
                facingOfEdge.turn(true)
            }
        } while(true)
    }

    fun Location.getChar(): Char? = field.getOrNull(this.row)?.getOrNull(this.column)
}

private class Maze(
    val field: List<CharArray>,
    val moves: List<Turn>,
    val initialMove: Int
) {
    private val facing: LoopedFacing = LoopedFacing()
    private var currentLocation = Location(0, field.first().indexOfFirst { it != ' ' })
    var edgeMap: Map<Edge, Edge>? = null

    fun moveThroughMaze(): Int {
        facing.setFacingTo(Facing.Down) // so the extra left turn turns to the right
        moves.forEach { move ->
            move.handleMove()
        }

        return 1000 * (currentLocation.row + 1) + 4 * (currentLocation.column + 1) + facing.currentFacing().value
    }

    private fun Turn.handleMove() {
        facing.turn(this.isLeft)
        repeat(this.amount) {
//            print()
            val canContinue = doStep(facing.currentFacing().step) //facing gets updated inside so recalc everytime, refactor needed
            if(!canContinue) return
        }
    }

    private fun Location.getChar(): Char? = field.getOrNull(this.row)?.getOrNull(this.column)

    private fun doStep(direction: Location): Boolean {
        val nextIntendedPosition = currentLocation + direction

        when(val nextSquare = nextIntendedPosition.getChar() ?: ' ') {
            ' ' -> {
                if(edgeMap != null) {
                    val (newPosition, newFacing) = edgeMap!![currentLocation to facing.currentOppositeFacing()]
                        ?: currentLocation.let { error("Location: ${it.row}, ${it.column} not in map") }
                    val newSquare = newPosition.getChar() ?: ' '
                    if(newSquare == '#') return false

                    currentLocation = newPosition
                    facing.setFacingTo(newFacing)
                    return true
                } else {
                    var checkingPosition = currentLocation
                    do {
                        val nextPosition = checkingPosition - direction
                        val char = nextPosition.getChar() ?: ' '
                        if(char == ' ') {
                            if(checkingPosition.getChar() == '#') break
                            currentLocation = checkingPosition
                        }

                        checkingPosition = nextPosition
                    } while (char != ' ')
                }
            }
            '#' -> return false
            '.' -> {
                currentLocation = nextIntendedPosition
                return true
            }
            else -> error("$nextSquare is not a valid character")
        }

        return true
    }

    fun print() {
        field.mapIndexed { rowIndex, chars ->
            chars.mapIndexed { columnIndex, char ->
                val output =
                    if(rowIndex == currentLocation.row && columnIndex == currentLocation.column) '0'
                    else char

                print(output)
            }
            print("\n")
        }
        println()
    }
}

private class LoopedFacing {
    private val turnDirections = listOf(Facing.Right, Facing.Down, Facing.Left, Facing.Up)
    private var pointer = 0

    fun turn(isLeft: Boolean, amount: Int = 1): Facing {
        pointer = (pointer + amount * if(isLeft) -1 else 1 ).mod(turnDirections.size)
        return turnDirections[pointer]
    }

    fun setFacingTo(to: Facing) {
        val index = turnDirections.indexOf(to)
        pointer = index
    }

    fun currentFacing() = turnDirections[pointer]

    fun currentOppositeFacing(): Facing {
        val index = (pointer + 2).mod(turnDirections.size)
        return turnDirections[index]
    }
}

private data class Location(val row: Int, val column: Int) {
    operator fun plus(other: Location) =
        Location(this.row + other.row, this.column + other.column)

    operator fun minus(other: Location) =
        Location(this.row - other.row, this.column - other.column)
}

private enum class Facing(val value: Int, val step: Location) {
    Right(0, Location(0, 1)),
    Down(1, Location(1, 0)),
    Left(2, Location(0, -1)),
    Up(3, Location(-1, 0)),
}

private data class Turn(val isLeft: Boolean, val amount: Int)


//private class CubeFace(
//    val adjacentFaces: List<CubeFacing>,
//    val inner: List<CharArray>? = List(3) { CharArray(3) { '.' } },
//) {
//    fun print() {
//        inner!!.forEach { line -> print(line) }
//        println()
//    }
//}
////x012
////0...
////1..<
////2...
//
//private class CubeSurface {
//    val facings = listOf(Up, Front, Left, Down, Back, Right)
//    val faces: Map<CubeFacing, CubeFace>
//
//    init {
//        faces = facings.associateWith { face ->
//            val opposite = face.oppositeFace()
//            val adjacentFaces = facings - listOf(face, opposite).toSet()
//            CubeFace(adjacentFaces, null)
//        }
//    }
//    fun CubeFacing.oppositeFace(): CubeFacing {
//        val index = facings.indexOf(this)
//        return facings[(index + 3) % facings.size]
//    }
//}
//
//private enum class CubeFacing {
//    Up,
//    Down,
//    Front,
//    Back,
//    Left,
//    Right
//}