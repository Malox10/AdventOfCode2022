fun main() {
    val input = readResourceLines("Day1.txt")
    val output = calculateCalories(input)
    println("The highest Calorie Count is: $output")
}

fun calculateCalories(list: List<String>): Int {
    var accumulator = 0
    var highestCalorieCount = 0

    list.map {
        if (it == "") {
            if (accumulator > highestCalorieCount) highestCalorieCount = accumulator
            accumulator = 0
        } else {
            accumulator += it.toInt()
        }

    }

    if (accumulator > highestCalorieCount) highestCalorieCount = accumulator
    return highestCalorieCount
}
