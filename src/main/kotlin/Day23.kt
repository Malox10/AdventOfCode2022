fun main() {
    val input = readResourceLines("Day23.txt")
//    val output = findEmptySpace(input)
    val output = findFinishedPositionSpace(input)
    println("The amount of unoccupied spaces is: $output")
}

typealias Elf = Pair<Int, Int>

private fun parseElves(input: List<String>): List<Elf> {
    val elves = input.flatMapIndexed { rowIndex, line ->
        line.mapIndexedNotNull { columnIndex, char ->
            if (char == '#') rowIndex to columnIndex else null
        }
    }

    return elves
}

fun findEmptySpace(input: List<String>): Int {
    val elves = parseElves(input)
    val unstableDiffusion = UnstableDiffusion(elves.toSet())
    return unstableDiffusion.diffuse()
}

fun findFinishedPositionSpace(input: List<String>): Int {
    val elves = parseElves(input)
    val unstableDiffusion = UnstableDiffusion(elves.toSet())
    return unstableDiffusion.diffuse(isPart2 = true)
}


class UnstableDiffusion(
    private var elfPositions: Set<Elf>,
) {
    fun diffuse(isPart2: Boolean = false): Int {
        if(!isPart2) {
            repeat(10) {
                print()
                doOneCycle()
            }
        } else {
            var count = 0
            do {
                count++
                print()
                val finished = doOneCycle()
            } while (!finished)

            return count
        }

        val minRow = elfPositions.minOf { it.first }
        val maxRow = elfPositions.maxOf { it.first }
        val rowDelta = maxRow - minRow

        val minColumn = elfPositions.minOf { it.second }
        val maxColumn = elfPositions.maxOf { it.second }
        val columnDelta = maxColumn - minColumn

        val boardSize = (rowDelta + 1) * (columnDelta + 1)
        return boardSize - elfPositions.size
    }

    private fun doOneCycle(): Boolean {
        val proposedMoves: MutableMap<Elf, MutableList<Elf>> = mutableMapOf()
        val edgeOffsets = LoopedOffsets.getOffsets()
        LoopedOffsets.rotate()

        val notMovingElves = elfPositions.filter { elf ->
            val occupiedSpaces = offsets.count { offset ->
                val newPosition = elf + offset
                elfPositions.contains(newPosition)
            }
            if (occupiedSpaces == 0) return@filter true
            val moveDirection = findMoveDirection(elf, edgeOffsets) ?: return@filter true

            val proposedPosition = elf + moveDirection.offset
            proposedMoves[proposedPosition]?.add(elf) ?: kotlin.run {
                proposedMoves[proposedPosition] = mutableListOf(elf)
            }

            false
        }

        if(notMovingElves.size == elfPositions.size) return true

        val newPositions = proposedMoves.flatMap { (intendedPosition, elves) ->
            if (elves.size > 1) elves.toList() else listOf(intendedPosition)
        }

        elfPositions = (notMovingElves + newPositions).toSet()
        return false
    }

    private fun findMoveDirection(elf: Elf, offsets: List<Pair<Direction, List<Pair<Int, Int>>>>): Direction? {
        offsets.forEach { (direction, checks) ->
            val occupiedSpaces = checks.count { check ->
                val potentialPosition = elf + check
                elfPositions.contains(potentialPosition)
            }
            if (occupiedSpaces == 0) {
                return direction
            }
        }

        return null
    }

    private fun print() {
        val minRow = elfPositions.minOf { it.first }
        val maxRow = elfPositions.maxOf { it.first }

        val minColumn = elfPositions.minOf { it.second }
        val maxColumn = elfPositions.maxOf { it.second }

        println("== End of Round ==")
        for (row in minRow..maxRow) {
            var output = ""
            for (column in minColumn..maxColumn) {
                val elf = row to column
                output += if (elfPositions.contains(elf)) '#' else '.'
            }
            println(output)
        }
        println()
    }

    companion object {
        private val offsets = setOf(
            Pair(-1, -1), Pair(-1, 0), Pair(-1, 1),
            Pair(0, -1), Pair(0, 1),
            Pair(1, -1), Pair(1, 0), Pair(1, 1)
        )
    }

    private object LoopedOffsets {
        private val checkMap: List<Pair<Direction, List<Pair<Int, Int>>>> = listOf(
            Direction.North to offsets.filter { it.first == -1 },
            Direction.South to offsets.filter { it.first == 1 },
            Direction.West to offsets.filter { it.second == -1 },
            Direction.East to offsets.filter { it.second == 1 },
        )
        private var pointer = 0

        fun rotate() {
            pointer = (pointer + 1) % checkMap.size
        }

        fun getOffsets() = (checkMap.indices).map {
            val value = checkMap[pointer]
            rotate()
            value
        }
    }

    private enum class Direction(val offset: Pair<Int, Int>) {
        North(-1 to 0),
        South(1 to 0),
        West(0 to -1),
        East(0 to 1),
    }
}

//typealias Offsets = List<Pair<Direction, List<Pair<Int, Int>>>>

private operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>) = this.first + other.first to this.second + other.second
private operator fun Pair<Int, Int>.minus(other: Pair<Int, Int>) =
    this.first - other.first to this.second - other.second
