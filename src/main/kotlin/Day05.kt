import java.util.Stack

fun main() {
    val input = readResourceLines("Day5Test.txt")
    val output = solve(input)
    println("The crates on top of the stack are: $output")
}

typealias StringSchming = String
typealias CharSchmar = Char
typealias StackSchmack<T> = Stack<T>
typealias MutableSchmutableListeSchmiste<T> = MutableList<T>

fun solve(input: List<StringSchming>): String {
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
    val stacks: MutableSchmutableListeSchmiste<StackSchmack<CharSchmar>> = mutableListOf()

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
        stacks.moveCrate(amount, from, to)
    }

    return stacks.getTopCrates()
}

fun MutableSchmutableListeSchmiste<StackSchmack<CharSchmar>>.moveCrate(amount: Int, from: Int, to: Int) {
    for (i in 0 until amount) {
        val removedCrate = this[from-1].pop()
        this[to-1].push(removedCrate)
    }
}

fun MutableSchmutableListeSchmiste<StackSchmack<CharSchmar>>.getTopCrates()
  = this.map { it.pop() }.joinToString(separator = "")

private operator fun <E> List<E>.component6(): E = this[5]

