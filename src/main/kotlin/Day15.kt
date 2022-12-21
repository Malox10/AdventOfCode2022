import kotlin.math.absoluteValue
import kotlin.system.measureTimeMillis

fun main() {
    val input = readResourceLines("Day15.txt")
//    val output = findLocationCountWithNoBeacons(input)
//    println("There are $output tiles without beacons")
    val output = findDistressBeacon(input)
    println("The Distress Beacons Signal is: $output")
}

private fun parseInput(input: List<String>): List<Sensor> {
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

    return sensors
}

fun findLocationCountWithNoBeacons(input: List<String>, target: Int = 2000000): Int {
    val sensors = parseInput(input)
    val ranges = sensors.mapNotNull { it.getBlockingRangeAt(target) }

    val simplifiedRanges = ranges.simplify()
    println(simplifiedRanges)
    val sum = simplifiedRanges.sumOf { it.end - it.start + 1 }
    return sum - sensors.map { it.closestBeacon }.toSet().count { it.second == target }
}

const val searchSpace = 4000000
fun findDistressBeacon(input: List<String>): Long {
    val sensors = parseInput(input)
    println(measureTimeMillis {
        (0..searchSpace).forEach { height ->
            val ranges = sensors.mapNotNull { it.getBlockingRangeAt(height) }.simplify()
            if (ranges.isEmpty()) error("range shouldn't be empty")
            val firstElement = ranges.first()
            if(ranges.size == 1 && firstElement.start <= 0 && firstElement.end >= searchSpace) return@forEach
            val firstInBoundRange = ranges.first { it.end > 0 }
            return (firstInBoundRange.end + 1).toLong() * searchSpace + height
        }
    })

    error("no beacon found")
}

data class Sensor(
    val location: Pair<Int, Int>,
    val closestBeacon: Pair<Int, Int>,
) {
    private val distance by lazy {
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

private fun Iterable<Range>.simplify(): Set<Range> {
    val collection = mutableListOf<Range>()
    val sortedRanges = this.sortedBy { it.start }

    var currentRange = sortedRanges.first()
    sortedRanges.drop(1).forEach { nextRange ->
//        val (firstRange, secondRange) = if(currentRange.start <= nextRange.start) currentRange to nextRange
//        else nextRange to currentRange
        if(!(currentRange.containsOrNextTo(nextRange.start) || currentRange.containsOrNextTo(nextRange.end))) {
            collection.add(currentRange)
            currentRange = nextRange
        }

        val newStart = if(currentRange.start <= nextRange.start) currentRange.start else nextRange.start
        val newEnd = if(currentRange.end >= nextRange.end) currentRange.end else nextRange.end

        currentRange = Range(newStart, newEnd)
    }

    collection.add(currentRange)
    return collection.toSet()
}

private operator fun Pair<Int, Int>.minus(other: Pair<Int, Int>) = this.first - other.first to this.second - other.second
private fun Range.containsOrNextTo(other: Int) = this.start - 1 <= other && this.end + 1 >= other