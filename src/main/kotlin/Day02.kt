fun main() {
    val input = readResourceLines("Day2.txt")
    val output = calculateScore(input)
    println("The expected Score is $output")
}

fun calculateScore(list: List<String>) = list.map {
        val (opponentString, youString) = it.split(" ")

        val you = when(youString) {
            "X" -> Shape.Rock
            "Y" -> Shape.Paper
            "Z" -> Shape.Scissors
            else -> error("Can't parse $youString")
        }

        val opponent = when(opponentString) {
            "A" -> Shape.Rock
            "B" -> Shape.Paper
            "C" -> Shape.Scissors
            else -> error("Can't parse $youString")
        }

        if(you == opponent) return@map you.value + 3

        val hasWon = when (you) {
            Shape.Rock -> opponent == Shape.Scissors
            Shape.Paper -> opponent == Shape.Rock
            Shape.Scissors -> opponent == Shape.Paper
        }

        you.value + if(hasWon) 6 else 0
    }.sum()

enum class Shape(val value: Int) {
    Rock(1),
    Paper(2),
    Scissors(3),
}