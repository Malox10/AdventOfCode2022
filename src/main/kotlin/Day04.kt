@file:Suppress("BooleanMethodIsAlwaysInverted")

fun main() {
    val input = readResourceLines("Day4.txt")
//    val output = findContainingWorkSchedules(input)
//    println("The amount of schedules where the schedules contain each other are: $output")
    val output = findOverlappingWorkSchedules(input)
    println("The amount of schedules that overlap each other are: $output")
}

@Suppress("unused")
fun findContainingWorkSchedules(input: List<String>) = input.count { line ->
    val (first, second) = line.toPairs()
    if (first.roomCount() > second.roomCount()) first.contains(second) else second.contains(first)
}

fun findOverlappingWorkSchedules(input: List<String>) = input.count { line ->
    val (first, second) = line.toPairs()

    val (bigger, smaller) =
        if (first.roomCount() > second.roomCount()) Pair(first, second) else Pair(second, first)
    bigger.toRange().contains(smaller.first) || bigger.toRange().contains(smaller.second)
}

fun Pair<Int, Int>.roomCount() = this.second - this.first

fun Pair<Int, Int>.contains(other: Pair<Int, Int>) = this.first <= other.first && this.second >= other.second

fun Pair<Int, Int>.toRange() = this.first..this.second

fun String.toPairs(): List<Pair<Int, Int>> = this.split(",").map { section ->
    val (start, end) = section.split("-").map { it.toInt() }
    Pair(start, end)
}
