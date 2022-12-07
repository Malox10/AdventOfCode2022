fun main() {
    val input = readResourceLines("Day3.txt")
    val output = findTheCommonElement(input)
    println("The sum of the common characters is: $output")
}

fun findTheCommonElement(input: List<String>) = input.map { line ->
        val firstHalf = line.substring(0, line.length/2).toSet()
        val secondHalf = line.substring(line.length/2).toSet()

        val commonCharacter = firstHalf.intersect(secondHalf).first()
        commonCharacter.code - if (commonCharacter.isUpperCase()) 38 else 96
    }.sum()