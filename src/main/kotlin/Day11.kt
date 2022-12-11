import java.util.*
import kotlin.math.floor

fun main() {
    val input = readResource("Day11.txt")
    val output = solve(input)
    println("$output")
}

fun solve(input: String): Int {
    val monkeyInfos = input.split("Monkey ").drop(1)
    val monkeys = monkeyInfos.map { monkeyInfo ->
        val (monkeyNumber, monkeyItems, monkeyOperation, monkeyTest, monkeyTestTrue, monkeyTestFalse) = monkeyInfo.lines()

        val number = monkeyNumber.split(":").first().trim().toInt()
        val startingItems =
            monkeyItems.trim().removePrefix("Starting items:")
                .split(",").map { it.trim().toInt() }

        val (operator, rightSide) =
            monkeyOperation.trim().removePrefix("Operation: new = old ").split(" ")

        val action =
            if (rightSide == "old") {
                { it: Int -> it * it }
            } else if (operator == "*") {
                { it: Int -> it * rightSide.toInt() }
            } else {
                { it: Int -> it + rightSide.toInt() }
            }

        val testDivisor = monkeyTest.trim().removePrefix("Test: divisible by").trim().toInt()
        val ifTrueThrowTo = monkeyTestTrue.trim().removePrefix("If true: throw to monkey").trim().toInt()
        val ifFalseThrowTo = monkeyTestFalse.trim().removePrefix("If false: throw to monkey").trim().toInt()
        val test = Test(testDivisor, ifTrueThrowTo, ifFalseThrowTo)

        val monkey = Monkey(number, LinkedList(startingItems), action, test)
        monkey
    }

    monkeys.forEach { monkey ->
        monkey.addMonkeyList(monkeys)
    }

    (1..20).forEach { _ ->
        monkeys.forEach { monkey ->
            monkey.throwAllItems()
        }
    }

    return monkeys.sortedBy { it.inspectCount }.map { it.inspectCount }.sorted().takeLast(2).reduce { a,b -> a * b  }
}

class Monkey(
    val number: Int,
    private var queue: Queue<Int>,
    val action: ((Int) -> Int),
    private val test: Test,
    private val monkeys: MutableList<Monkey> = mutableListOf()
    ) {

    var inspectCount = 0

    fun addMonkeyList(list: List<Monkey>) {
        monkeys += list
    }

    fun throwAllItems() {
        while(!queue.isEmpty()) {
            handleItem()
        }
    }

    private fun handleItem() {
        if (queue.isEmpty()) return
        val item = inspectItem().reduceWorry()
        val recipient = test.getRecipient(item)
        passItem(item, recipient)
    }

    private fun receiveItem(item: Int) = queue.add(item)

    private fun passItem(item: Int, recipient: Int) = monkeys[recipient].receiveItem(item)

    private fun inspectItem() = action(queue.remove()).also { inspectCount++ }

    private fun Int.reduceWorry() = floor((this / 3).toDouble()).toInt()

}

data class Test(
    val divisor: Int,
    val IfTrueThrowTo: Int,
    val IfFalseThrowTo: Int
) {
    fun getRecipient(worryLevel: Int): Int {
       return if(worryLevel % divisor == 0) IfTrueThrowTo else IfFalseThrowTo
    }
}

private operator fun <E> List<E>.component6(): E = this[5]