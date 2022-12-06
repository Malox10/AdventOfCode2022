fun main() {
    val input = readResourceLines("Day2.txt")
//    val output = calculateScores(input)
    val output = calculateScorePart2(input)
    println("The expected Score is $output")
}

fun calculateScores(list: List<String>) = list.sumOf { calculateScore(it) }
fun calculateScore(element: String): Int {
        val (opponentString, youString) = element.split(" ")

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
            else -> error("Can't parse $opponentString")
        }

        if(you == opponent) return you.value + 3

        val hasWon = when (you) {
            Shape.Rock -> opponent == Shape.Scissors
            Shape.Paper -> opponent == Shape.Rock
            Shape.Scissors -> opponent == Shape.Paper
        }

        return you.value + if(hasWon) 6 else 0
}

fun calculateScorePart2(list: List<String>): Int = list.sumOf { element ->
    val (opponentString, strategyString) = element.split(" ")

    val opponent = when(opponentString) {
        "A" -> Shape.Rock
        "B" -> Shape.Paper
        "C" -> Shape.Scissors
        else -> error("Can't parse $opponentString")
    }

    val shouldWin = when(strategyString) {
        "X" -> false
        "Y" -> return@sumOf calculateScore("${opponent.A} ${opponent.X}")
        "Z" -> true
        else -> error("Can't parse $strategyString")
    }

    val you = when(opponent) {
        Shape.Rock -> if(shouldWin) Shape.Paper else Shape.Scissors
        Shape.Paper -> if(shouldWin) Shape.Scissors else Shape.Rock
        Shape.Scissors -> if(shouldWin) Shape.Rock else Shape.Paper
    }

    calculateScore("${opponent.A} ${you.X}")
}

enum class Shape(val value: Int, val A: String, val X: String) {
    Rock(1, "A", "X"),
    Paper(2, "B", "Y"),
    Scissors(3, "C", "Z"),
}
