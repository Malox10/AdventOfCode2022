fun main() {
    val input = readResourceLines("Day4.txt")
    val output = findContainingWorkSchedules(input)
    println("The amount of schedules where the schedules contain each other are: $output")
}

fun findContainingWorkSchedules(input: List<String>) = input.count { line ->
    val (first, second) = line.split(",").map { section ->
        val (start, end) = section.split("-").map { it.toInt() }
        Pair(start, end)
    }

    if (first.roomCount() > second.roomCount()) first.contains(second) else second.contains(first)
}

fun Pair<Int, Int>.roomCount() = this.second - this.first

fun Pair<Int, Int>.contains(other: Pair<Int, Int>) = this.first <= other.first && this.second >= other.second
