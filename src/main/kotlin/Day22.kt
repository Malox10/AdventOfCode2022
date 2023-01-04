fun main() {
    val input = readResource("Day22.txt")
    val output = findPassword(input)
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

private class Maze(
    val field: List<CharArray>,
    val moves: List<Turn>,
    val initialMove: Int,
) {
    val facing: LoopedFacing = LoopedFacing()
    var currentLocation = Location(0, field.first().indexOfFirst { it != ' ' })

    fun moveThroughMaze(): Int {
        repeat(3) { facing.turn(true) } //prepend a left turn to have a pairs, and this is to equalize that extra left turn
        moves.map { move ->
            move.handleMove()
        }

        return 1000 * (currentLocation.row + 1) + 4 * (currentLocation.column + 1) + facing.currentFacing().value
    }

    fun Turn.handleMove() {
        val direction = facing.turn(this.isLeft).step
        repeat(this.amount) {
//            print()
            val canContinue = doStep(direction)
            if(!canContinue) return
        }
    }

    fun Location.getChar(): Char? = field.getOrNull(this.row)?.getOrNull(this.column)

    fun doStep(direction: Location): Boolean {
        val nextIntendedPosition = currentLocation + direction

        when(val nextSquare = nextIntendedPosition.getChar() ?: ' ') {
            ' ' -> {
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

private class LoopedFacing() {
    private val turnDirections = listOf(Facing.Right, Facing.Down, Facing.Left, Facing.Up)
    private var pointer = 0

    fun turn(isLeft: Boolean): Facing {
        pointer = (pointer + if(isLeft) -1 else 1).mod(turnDirections.size)
        return turnDirections[pointer]
    }

    fun currentFacing() = turnDirections[pointer]
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