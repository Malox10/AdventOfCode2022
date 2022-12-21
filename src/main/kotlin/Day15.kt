import kotlin.math.absoluteValue

fun main() {
    val input = readResourceLines("Day15.txt")
    val output = findLocationCountWithNoBeacons(input)
    println("There are $output tiles without beacons")
}

fun findLocationCountWithNoBeacons(input: List<String>, target: Int = 2000000): Int {
    val pairs = input.map { line ->
        val(_, sensor, beacon) = line.split("at x=").map { it.replace(":", ", y=") }
        sensor to beacon
    }

    val sensors = pairs.map { pair ->
        val (sensor, beacon) = pair.toList().map { entry ->
            val (x, y) = entry.split(", y=").map { it.trim() }
            x.toInt() to y.toInt()
        }
        Sensor(sensor, beacon)
    }
//    val ranges = sensors.mapNotNull { it.getBlockingRangeAt(10) }
//    println

    val ranges = sensors.mapNotNull {
        val range = it.getBlockingRangeAt(target)
        println("$it, ${it.distance}: $range")
        range
    }

    val simplifiedRanges = ranges.simplify()
    println(simplifiedRanges)
    val sum = simplifiedRanges.sumOf { it.end - it.start + 1 }
    return sum - sensors.map { it.closestBeacon }.toSet().count { it.second == target }
}

data class Sensor(
    val location: Pair<Int, Int>,
    val closestBeacon: Pair<Int, Int>,
) {
    val distance by lazy {
        val delta = location - closestBeacon
        delta.first.absoluteValue + delta.second.absoluteValue
    }

    //returns the range or null if it doesn't block any tiles
    fun getBlockingRangeAt(targetStripHeight: Int): Range? {
        val (x, y) = this.location
        val distanceToTarget = (targetStripHeight - y).absoluteValue
        if(distanceToTarget > distance) return null

        val remainingDistance = distance - distanceToTarget
        return Range(x - remainingDistance, x + remainingDistance)
    }
}

data class Range(val start: Int, val end: Int)

private fun Iterable<Range>.simplify(): List<Range> {
    val collection = mutableSetOf<Range>()
    val sortedRanges = this.sortedBy { it.start }

    var currentRange = sortedRanges.first()
    sortedRanges.drop(1).forEach { nextRange ->
//        val (firstRange, secondRange) = if(currentRange.start <= nextRange.start) currentRange to nextRange
//        else nextRange to currentRange
        if(!(currentRange.contains(nextRange.start) || currentRange.contains(nextRange.end))) {
            collection.add(currentRange)
            currentRange = nextRange
        }

        val newStart = if(currentRange.start <= nextRange.start) currentRange.start else nextRange.start
        val newEnd = if(currentRange.end >= nextRange.end) currentRange.end else nextRange.end

        currentRange = Range(newStart, newEnd)
    }

    collection.add(currentRange)
    return collection.toList()
}

private operator fun Pair<Int, Int>.minus(other: Pair<Int, Int>) = this.first - other.first to this.second - other.second
private fun Range.contains(other: Int) = this.start <= other && this.end >= other