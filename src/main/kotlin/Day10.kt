fun main() {
    val input = readResourceLines("Day10.txt")
    val output = findSignalStrength(input)
    println("The combined signal strength is: $output")
}

fun findSignalStrength(input: List<String>): Int {
    val instructions = input.map { instructionText ->
        when {
            instructionText.startsWith("noop") -> Instruction.Noop()
            instructionText.startsWith("addx") -> {
                val (_, value) = instructionText.split(" ")
                Instruction.AddX(value.toInt())
            }
            else -> error("Can't parse instruction $instructionText")
        }
    }

    val cpu = CPU()
    instructions.forEach { cpu.doInstruction(it) }
    return cpu.signalAccumulator
}

sealed class Instruction() {
    class Noop: Instruction()
    data class AddX(val value: Int): Instruction()
}

class CPU {
    var cycle: Int = 0
    var xRegister = 1
    var signalAccumulator = 0

    private fun finishCycle() {
        draw()
        cycle++
        if(cycle % 40 == 20) {
            signalAccumulator += xRegister * cycle
        }
    }

    fun doInstruction(instruction: Instruction) {
        when (instruction) {
            is Instruction.Noop -> finishCycle()
            is Instruction.AddX -> {
                finishCycle()
                finishCycle()
                xRegister += instruction.value
            }
        }
    }

    private fun draw() {
        val rayPosition = cycle % 40
        val registerRange = xRegister-1..xRegister+1

        if(rayPosition == 0) print("\n")
        if(registerRange.contains(rayPosition)) print("#") else print(".")
    }
}
