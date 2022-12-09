fun main() {
    val input = readResourceLines("Day3.txt")
//    val output = findTheCommonElement(input)
    val output = findTheBadge(input)
    println("The sum of the common characters is: $output")
}

@Suppress("unused")
fun findTheCommonElement(input: List<String>) = input.sumOf { line ->
    val firstHalf = line.substring(0, line.length / 2).toSet()
    val secondHalf = line.substring(line.length / 2).toSet()

    val commonCharacter = firstHalf.intersect(secondHalf).first()
    commonCharacter.toPriority()
}

fun Char.toPriority() = this.code - if (this.isUpperCase()) 38 else 96

fun findTheBadge(input: List<String>) =
    input
        .chunked(3)
        .sumOf { chunk ->
            chunk
                .map { it.toSet() }
                .reduce { a, b -> a.intersect(b) }
                .first()
                .toPriority()
        }
