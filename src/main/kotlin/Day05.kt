import java.util.Stack

fun main() {
    val input = readResourceLines("Day5.txt")
    val output = findTopCrates(input)
    println("The crates on top of the stack are: $output")
}

fun findTopCrates(input: List<String>): String {
    val instructions = mutableListOf<String>()
    val craneGame = mutableListOf<String>()

    input.reversed().forEach { line ->
        if (line.startsWith("move")) {
            instructions.add(line)
        } else {
            craneGame.add(line)
        }
    }

    instructions.reverse()

    craneGame.removeFirst()
    val stacks: MutableList<Stack<Char>> = mutableListOf()

    for (column in 0 until craneGame.first().length) {
        if(craneGame[0][column] == ' ') continue

        val stack = Stack<Char>()
        for(row in 1 until craneGame.size) {
            if (craneGame[row].length < column) break
            val crate = craneGame[row][column]
            if (crate.isLetter()) stack.add(crate)
        }

        stacks.add(stack)
    }

    instructions.forEach { instruction ->
        val (_, amount, _, from, _, to) = instruction.split(' ')
            .map { it.toIntOrNull() ?: 0 }
        stacks.moveCratePart2(amount, from, to)
    }

    return stacks.getTopCrates()
}

@Suppress("unused")
fun MutableList<Stack<Char>>.moveCrate(amount: Int, from: Int, to: Int) {
    for (i in 0 until amount) {
        val removedCrate = this[from-1].pop()
        this[to-1].push(removedCrate)
    }
}

fun MutableList<Stack<Char>>.moveCratePart2(amount: Int, from: Int, to: Int) {
    val cratesToMove = mutableListOf<Char>()

    for (i in 0 until amount) cratesToMove.add(this[from-1].pop())
    cratesToMove.reversed().forEach { this[to-1].push(it) }
}

fun MutableList<Stack<Char>>.getTopCrates()
  = this.map { it.pop() }.joinToString(separator = "")

private operator fun <E> List<E>.component6(): E = this[5]

