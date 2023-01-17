import kotlin.math.pow

fun main() {
    val input = readResourceLines("Day25.txt")
    val output = findSNAFUNumber(input)
    println("The SNAFU code is: $output")
}

fun findSNAFUNumber(input: List<String>): String {
    val map = input.associateWith { it.toDecimal() }

    val sum = map.values.sum()
    return decimalToSNAFU(sum)
}

fun decimalToSNAFU(target: Long): String {
    if(target <= 2) return target.toString()
    var SNAFU = "2"

    do {
        SNAFU += "2"
        val snafuDecimal = SNAFU.toDecimal()
        val isSmaller = snafuDecimal < target
    } while(isSmaller)

    println(SNAFU)
    val onePrefix = "1" + SNAFU.drop(1)
    var upperBound = (if(onePrefix.toDecimal() > target) onePrefix else SNAFU).toCharArray()
    var lowerBound = (SNAFU.first() + "=".repeat(SNAFU.length - 1)).toCharArray()

    if(upperBound.toDecimal() == target) return String(upperBound)
    if(lowerBound.toDecimal() == target) return String(lowerBound)
    for(index in 1 until SNAFU.length) {
        do {
            if(lowerBound[index] == '2') {
                upperBound = lowerBound.sliceArray(0..index) + CharArray(SNAFU.length - index - 1) { '2' }
                break
            }
            val increasedDigit = lowerBound[index].increase()
            val newUpperBound = lowerBound.copyOf()
            newUpperBound[index] = increasedDigit

            val newUpperBoundValue = newUpperBound.toDecimal()
            if(newUpperBoundValue == target) return String(newUpperBound)

            val isBigger = newUpperBoundValue > target
            if(isBigger) {
                upperBound = newUpperBound
            } else {
                lowerBound = newUpperBound
            }
        } while(!isBigger)

        println(upperBound)
    }

    error("No SNAFU string found")
}

val mapping = mapOf(
    '=' to '-',
    '-' to '0',
    '0' to '1',
    '1' to '2',
    '2' to '2',
)
private fun Char.increase() = mapping[this]!!

private fun CharArray.toDecimal() = this.joinToString("").toDecimal()
private fun String.toDecimal(): Long {
    return this.reversed().mapIndexed { index, char ->
        val base = 5.0.pow(index.toDouble())
        val value = base * when(char) {
            '2' -> 2
            '1' -> 1
            '0' -> 0
            '-' -> -1
            '=' -> -2
            else -> error("$char is not a valid SNAFU character")
        }

        value.toLong()
    }.sum()
}
