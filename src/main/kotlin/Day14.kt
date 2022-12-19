fun main() {
    val input = readResourceLines("Day14.txt")
    val output = findNumerOfRestingSandCorns(input)
    println("The number of resting Sandcorns is: $output")
}

fun findNumerOfRestingSandCorns(input: List<String>): Int {
    val sandSimulator = parseInput(input)
    sandSimulator.print()
    return sandSimulator.simulateSand()
}

private fun parseInput(input: List<String>): SandSimulator {
    val stringPairs = input.flatMap { it.split("->") }.map { it.trim() }
    val pairs = stringPairs.map {
        val (x, y) = it.split(",")
        x.toInt() to y.toInt()
    }

    val coordList = pairs.unzip()

    val xMin = coordList.first.min()
    val xMax = coordList.first.max()
    val width = xMax - xMin + 1

    val yMax = coordList.second.max()
    val offset = xMin to 0

    val grid = List(yMax + 1) { CharArray(width) { '.' } }

    return SandSimulator(offset, grid, input)
}

class SandSimulator(
    private val offset: Pair<Int, Int>,
    private val grid: List<CharArray>,
    val input: List<String>
) {
    private val sandStartingPosition = (500 to 0) - offset
    var sandPosition: Pair<Int, Int> = sandStartingPosition
    var restingSandCount: Int = 0

    init {
        input.forEach { line ->
            val pairs = line.split("->").map {
                val (x, y) = it.trim().split(",")
                x.toInt() to y.toInt()
            }

            placeRockLine(pairs)
        }
    }

    private fun placeRockLine(rocks: List<Pair<Int, Int>>) {
        val pairs = rocks.windowed(2)
        pairs.map {
            val (first, second) = it
            val start = first - offset
            val end = second - offset


            val columnRange = if(start.first < end.first) start.first..end.first else end.first..start.first
            val rowRange = if(start.second < end.second) start.second..end.second else end.second..start.second
            for(column in columnRange) {
                for (row in rowRange) {
                    grid[row][column] = '#'
                    println("$row, $column")
                }
            }
        }
    }

    fun simulateSand(): Int {
        do {
            val isInBounds = simulateOneSandCorn()
            if(isInBounds) {
                restingSandCount++
                spawnSand()
                print()
            }
        } while(isInBounds)

        this.print()
        return restingSandCount
    }

    private fun simulateOneSandCorn(): Boolean {
        while(true) {
            val cameToRest = simulateOneCycle() ?: return false
            if(cameToRest) return true
        }
    }

    private fun simulateOneCycle(): Boolean? {
        grid[sandPosition.second][sandPosition.first] = '.'
        val result = tryRest()

        //this.print()
        return result
    }

    private fun tryRest(): Boolean? {
        val down = sandPosition + Offset.Down.value
        if (!grid.isInBounds(down)) {
            return null
        }
        if (grid[down.second][down.first] == '.') {
            sandPosition += Offset.Down.value
            grid[sandPosition.second][sandPosition.first] = '+'
            return false
        }

        //incoming garbage code
        val downLeft = sandPosition + Offset.DownLeft.value
        if (!grid.isInBounds(downLeft)) {
            return null
        }
        if (grid[downLeft.second][downLeft.first] == '.') {
            sandPosition += Offset.DownLeft.value
            grid[downLeft.second][downLeft.first] = '+'
            return false
        }

        val downRight = sandPosition + Offset.DownRight.value
        if (!grid.isInBounds(downRight)) {
            return null
        }
        if (grid[downRight.second][downRight.first] == '.') {
            sandPosition += Offset.DownRight.value
            grid[downRight.second][downRight.first] = '+'
            return false
        }

        grid[down.second - 1][down.first] = 'o'
        return true
    }

    private fun spawnSand() {
        sandPosition = sandStartingPosition
    }

    fun print() = grid.forEachIndexed { index, line -> line.forEach { print(it) }.also { print("$index\n") } }

    private fun List<CharArray>.isInBounds(index: Pair<Int, Int>): Boolean {
        if(this.isEmpty()) return false
        if(index.second < 0 || index.first < 0) return false

        val rowAmount = this.size
        val columnAmount = this.first().size

        if(index.second >= rowAmount || index.first >= columnAmount) return false
        return true
    }

}

private operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>) = this.first + other.first to this.second + other.second
private operator fun Pair<Int, Int>.minus(other: Pair<Int, Int>) = Pair(this.first - other.first, this.second - other.second)

enum class Offset(val value: Pair<Int, Int>) {
    Down(0 to 1),
    DownLeft(-1 to 1),
    DownRight(1 to 1),
}
