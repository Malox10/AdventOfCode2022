import java.lang.Integer.max

fun main() {
    val input = readResource("Day17.txt")
//    val output = findTowerHeight(input)
    val output = findExtremeTowerHeight(input)
    println("The height of tower is: $output")
}

fun findTowerHeight(input: String): Int {
    val pieces = parsePentaminos()
    val pattern = parseJetPattern(input)
    val board = Board(pieces, pattern, 2022)

    println(pieces.size)
    println(pattern.size)

    board.simulate()
    return board.height
}

const val targetCycleAmount = 1_000_000_000_000L
//const val targetCycleAmount = 2022L
fun findExtremeTowerHeight(input: String): Long {
    val pieces = parsePentaminos()
    val pattern = parseJetPattern(input)

    val topSequences = mutableSetOf<Top>()
    val lengths = mutableListOf<Data>()
    val cycles = mutableListOf<Int>()

    var lastTop: List<CharArray>? = null
    var firstTopHeight = 0
    var loopCount = 0
    //do {
    val board = Board(pieces, pattern, null, null)

    val (new, old) = board.simulate()
    val cycleLength = new.cycle - old.cycle
    val cycleHeight = new.height - old.height
    val preCycleHeight = old.height
    println("cycle length is: $cycleLength and height of the cycle is: $cycleHeight")

    //500, targeted cycle
    //20 - 70 = 50, from to
    //470, end of last cycle
    //so 30 missing cycles, but 500 % 50 = 0
    val cycleAmount = (targetCycleAmount - old.cycle).floorDiv(cycleLength)
    val rest = (targetCycleAmount - old.cycle) % cycleLength

    val lastCycleData = board.topDataMap.values.find { it.cycle == (old.cycle + rest).toInt() }!!
    val postCycleHeight = lastCycleData.height - old.height

    val heightFromAllCycles = cycleHeight * (cycleAmount - 0)

    return heightFromAllCycles + postCycleHeight + preCycleHeight
}

data class Data(val x: Int, val y: Int, val z: Int)

class Board(
    val pieces: LoopedList<Pentamino>,
    val pattern: LoopedList<PushDirection>,
    private val cycleCount: Int?,
    val startingBoard: List<CharArray>? = null,
) {
    val board: MutableList<CharArray> = MutableList((cycleCount ?: 100000) * 4) { CharArray(7) { '.' } }
    var height = 0

    var piecePointer: Pair<Int, Int> = height + 3 to 2
    lateinit var piece: Pentamino

    init {
        setStartingBoard()
    }

    private fun setStartingBoard() {
        if (startingBoard == null) return
        startingBoard.forEachIndexed { index, list ->
            board[index] = list
        }
        updateHeight()
    }

    private fun getTop(): Top? {
        val heightMask = MutableList(7) { -1 }
        var heightDelta = 0
        while (heightMask.contains(-1)) {
            val newHeight = height - heightDelta
            if (newHeight < 0) return null
            board[newHeight].forEachIndexed { column, char ->
                if (char == '#' && heightMask[column] == -1) heightMask[column] = heightDelta
            }
            heightDelta++
        }

        return Top(heightMask, pieces.pointer, pattern.pointer)
    }

    fun printBoard(debug: Boolean = false): List<String> {
        if (!debug) return listOf()
        val maxPrintHeight = 40

        val startHeight = max(height - (maxPrintHeight - 5), 0)

        val lines = (startHeight..startHeight + maxPrintHeight).reversed().map { row ->
            var output = "|"
            board[row].forEachIndexed { column, char ->
                val position = (row to column) - piecePointer
                output += if (piece.offsets.contains(position)) '@' else board[row][column]
            }
            output += "|"
            println(output)
            output
        }
        println("+-------+\n")
        return lines
    }

    val topSequences = mutableSetOf<Top>()
    val topDataMap = mutableMapOf<Top, TopData>()
    fun simulate(): Pair<TopData, TopData> {
        val action = {
            simulatePiece()
            updateHeight()
        }

        when (cycleCount) {
            null -> {
                var cycle = 0
                while (true) {
                    action()
                    cycle++
                    val top = getTop() ?: continue
                    val topData = TopData(height, cycle)
                    if (topDataMap.contains(top)) { //use .let pattern
                        return topData to topDataMap[top]!!
                    } else {
                        topSequences.add(top)
                        topDataMap[top] = topData
                    }
                }
            }
            else -> repeat(cycleCount) { action() }
        }

        return TopData(height, cycleCount!!) to TopData(0, 0)
    }

    private fun simulatePiece() {
        spawnPiece()
        var printBoard = false
        if (startingBoard != null) {
            printBoard = false
        }

        printBoard(printBoard)
        do {
            pushByJet()
            printBoard(printBoard)
            val didPieceContinue = fall()
            printBoard(printBoard)
        } while (didPieceContinue)
        stopPiece()
    }

    private fun updateHeight() {
        while (lineHasPieces()) {
            height++
        }
    }

    private fun lineHasPieces(): Boolean {
        return board[height].count { it == '.' } != 7
    }

    private fun stopPiece() =
        piece.offsets.forEach { board[it.first + piecePointer.first][it.second + piecePointer.second] = '#' }

    private fun fall(): Boolean {
        piecePointer += PushDirection.Down.value
        val isValid = isValidPosition()
        if (!isValid) piecePointer -= PushDirection.Down.value
        return isValid
    }

    private fun pushByJet() {
        val pushDirection = pattern.next()
        piecePointer += pushDirection.value
        if (!isValidPosition()) piecePointer -= pushDirection.value
    }

    private fun isValidPosition(): Boolean {
        piece.offsets.forEach {
            val newPosition = it + piecePointer
            if (!newPosition.isEmptyAndInBounds()) return false
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

    private fun movePiecePointerToTop() {
        piecePointer = height + 3 to 2
    }
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
    LoopedList(input.map { if (it == '<') PushDirection.Left else PushDirection.Right })

data class Pentamino(val offsets: List<Pair<Int, Int>>)

data class Top(val depths: List<Int>, val pieceCycle: Int, val patternCycle: Int)
data class TopData(val height: Int, val cycle: Int)

class LoopedList<T>(private val list: List<T>) {
    var pointer = 0
    val size get() = list.size

    fun next(): T {
        val result = list[pointer]
        pointer++
        if (pointer >= list.size) pointer = 0
        return result
    }
}

enum class PushDirection(val value: Pair<Int, Int>) {
    Left(0 to -1),
    Right(0 to +1),
    Down(-1 to 0)
}

private operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>) = this.first + other.first to this.second + other.second
private operator fun Pair<Int, Int>.minus(other: Pair<Int, Int>) =
    this.first - other.first to this.second - other.second