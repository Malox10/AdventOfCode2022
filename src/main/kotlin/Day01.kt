fun main() {
    val input = readResourceLines("Day1.txt")

//    val output = calculateCalories(input)
//    println("The highest Calorie Count is: $output")

    val amount = 3
    val output = countCaloriesPart2(input, 3)
    println("The highest $amount Elves have $output Calories in total!")
}

//fun calculateCalories(list: List<String>): Int {
//    var accumulator = 0
//    var highestCalorieCount = 0
//
//    list.map {
//        if (it == "") {
//            if (accumulator > highestCalorieCount) highestCalorieCount = accumulator
//            accumulator = 0
//        } else {
//            accumulator += it.toInt()
//        }
//    }
//
//    if (accumulator > highestCalorieCount) highestCalorieCount = accumulator
//    return highestCalorieCount
//}

fun countCaloriesPart2(list: List<String>, amount: Int): Int {
    val summedCalories = mutableListOf<Int>()

    var accumulator = 0
    list.map {
        if (it != "") accumulator += it.toInt()
        else {
            summedCalories.add(accumulator)
            accumulator = 0
        }
    }

    return summedCalories.sorted().takeLast(amount).sum()
}
