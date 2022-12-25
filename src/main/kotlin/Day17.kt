fun main() {
    val input = readResource("Day17.txt")
    val output = solve(input)
    println("The height of tower is: $output")
}

fun solve(input: String): Int {
    val pieces = parsePentaminos()
    val pattern = parseJetPattern(input)
    val board = Board(pieces, pattern)

    board.simulate()
    return board.height
}

class Board(
    private val pieces: LoopedList<Pentamino>,
    private val pattern: LoopedList<PushDirection>,
) {
    private val board: List<CharArray> = List(2022 * 4) { CharArray(7) { '.' } }

    var height = 0
    var piecePointer: Pair<Int, Int> = height + 3 to 2
    lateinit var piece: Pentamino

    private fun printBoard() {
        val maxPrintHeight = 8

        for (row in (0..maxPrintHeight).reversed()) {
            print("|")
            board[row].forEachIndexed { column, char ->
                val position = (row to column) - piecePointer
                val output = if(piece.offsets.contains(position)) '@' else board[row][column]
                print(output)
            }
            print("|\n")
        }
        println("+-------+\n")
    }

    fun simulate() {
        repeat(2022) {
            simulatePiece()
            recalculateHeight()
        }
    }

    private fun simulatePiece() {
        spawnPiece()
        printBoard()
        do {
            pushByJet()
            printBoard()
            val didPieceContinue = fall()
            printBoard()
        } while (didPieceContinue)
        stopPiece()
    }

    private fun recalculateHeight() {
        while (lineHasPieces()) {
            height++
        }
    }

    private fun lineHasPieces(): Boolean { return board[height].count { it == '.' } != 7 }

    private fun stopPiece() = piece.offsets.forEach { board[it.first + piecePointer.first][it.second + piecePointer.second] = '#'}

    private fun fall(): Boolean {
        piecePointer += PushDirection.Down.value
        val isValid = isValidPosition()
        if(!isValid) piecePointer -= PushDirection.Down.value
        return isValid
    }

    private fun pushByJet() {
        val pushDirection = pattern.next()
        piecePointer += pushDirection.value
        if(!isValidPosition()) piecePointer -= pushDirection.value
    }

    private fun isValidPosition(): Boolean {
        piece.offsets.forEach {
            val newPosition = it + piecePointer
            if(!newPosition.isEmptyAndInBounds()) return false
        }
        return true
    }

    private fun Pair<Int, Int>.isEmptyAndInBounds(): Boolean {
        return 0 <= this.second
                && this.second < board.first().size
                && board.getOrNull(this.first)?.getOrNull(this.second) == '.'
    }

    private fun spawnPiece() {
        movePiecePointerToTop()
        piece = pieces.next()
    }

    private fun movePiecePointerToTop() { piecePointer = height + 3 to 2 }
}

private fun parsePentaminos(): LoopedList<Pentamino> {
    val input =
    "####\n" +
    "\n" +
    ".#.\n" +
    "###\n" +
    ".#.\n" +
    "\n" +
    "###\n" +
    "..#\n" +
    "..#\n" +
    "\n" +
    "#\n" +
    "#\n" +
    "#\n" +
    "#\n" +
    "\n" +
    "##\n" +
    "##"

    val piecesString = input.split("\n\n")
    val pentaminos = piecesString.map { piece ->
        piece.split("\n").flatMapIndexed { row, line ->
            line.mapIndexedNotNull { column, char ->
                if (char == '#') row to column else null
            }
        }
    }.map { Pentamino(it) }

    return LoopedList(pentaminos)
}

private fun parseJetPattern(input: String) =
    LoopedList(input.map { if(it == '<') PushDirection.Left else PushDirection.Right })

data class Pentamino (val offsets: List<Pair<Int, Int>>)

class LoopedList<T>(private val list: List<T>) {
    var pointer = 0

    fun next(): T {
        val result = list[pointer]
        pointer++
        if(pointer >= list.size) pointer = 0
        return result
    }
}

enum class PushDirection(val value: Pair<Int, Int>) {
    Left(0 to -1),
    Right(0 to +1),
    Down(-1 to 0)
}

private operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>) = this.first + other.first to this.second + other.second
private operator fun Pair<Int, Int>.minus(other: Pair<Int, Int>) = this.first - other.first to this.second - other.second