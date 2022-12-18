@file:Suppress("unused")
import java.util.*
import kotlin.math.floor

fun main() {
    val input = readResource("Day11.txt")
    val output = findAmountOfMonkeyBusiness(input)
    println("$output")
}

fun findAmountOfMonkeyBusiness(input: String): Long {
    val monkeyInfos = input.split("Monkey ").drop(1)
    val monkeys = monkeyInfos.map { monkeyInfo ->
        val (monkeyNumber, monkeyItems, monkeyOperation, monkeyTest, monkeyTestTrue, monkeyTestFalse) = monkeyInfo.lines()

        val number = monkeyNumber.split(":").first().trim().toLong()
        val startingItems =
            monkeyItems.trim().removePrefix("Starting items:")
                .split(",").map { it.trim().toLong() }

        val (operator, rightSide) =
            monkeyOperation.trim().removePrefix("Operation: new = old ").split(" ")

        val testDivisor = monkeyTest.trim().removePrefix("Test: divisible by").trim().toLong()
        val ifTrueThrowTo = monkeyTestTrue.trim().removePrefix("If true: throw to monkey").trim().toLong()
        val ifFalseThrowTo = monkeyTestFalse.trim().removePrefix("If false: throw to monkey").trim().toLong()
        val test = Test(testDivisor, ifTrueThrowTo, ifFalseThrowTo)

        val action =
            if (rightSide == "old") {
                { it: Long -> it * it }
            } else if (operator == "*") {
                { it: Long -> it * rightSide.toLong() }
            } else {
                { it: Long -> it + rightSide.toLong() }
            }

        val monkey = Monkey(number, LinkedList(startingItems), action, test)
        monkey
    }

    val totalDivisor = monkeys.map { monkey ->
        monkey.test.divisor
    }.reduce { a,b -> a * b}

    monkeys.forEach { monkey ->
        monkey.addStressReducerModulo(totalDivisor)
        monkey.addMonkeyList(monkeys)
    }

    (1..10000).map { index ->
        val queues = monkeys.map { monkey ->
            monkey.throwAllItems()
            monkey.queue
        }

        if(listOf(1, 20, 1000, 2000, 3000, 4000).contains(index)) {
            monkeys.forEach { monkey ->
                println("Monkey ${monkey.number} inspected items ${monkey.inspectCount} times")
            }
            println("== After round $index ==")
        }

        queues
    }

    return monkeys.sortedBy { it.inspectCount }.map { it.inspectCount }.sorted().takeLast(2).reduce { a: Long, b: Long -> a * b  }
}

class Monkey(
    val number: Long,
    var queue: Queue<Long>,
    val action: (Long) -> Long,
    val test: Test,
    private val monkeys: MutableList<Monkey> = mutableListOf(),
    ) {

    private var stressReducerModulo: Long = 0
    var inspectCount: Long = 0

    fun addMonkeyList(list: List<Monkey>) {
        monkeys += list
    }
    fun addStressReducerModulo(input: Long) {
        stressReducerModulo = input
    }

    fun throwAllItems() {
        while(!queue.isEmpty()) {
            handleItem()
        }
    }

    private fun handleItem() {
        if (queue.isEmpty()) return
        val item = inspectItem().reduceWorryPart2()
        val recipient = test.getRecipient(item)
        passItem(item, recipient)
    }

    private fun receiveItem(item: Long) = queue.add(item)
    private fun passItem(item: Long, recipient: Long) = monkeys[recipient.toInt()].receiveItem(item)
    private fun inspectItem() = action(queue.remove()).also { inspectCount++ }
    private fun Long.reduceWorry() = floor((this / 3).toDouble()).toLong()
    private fun Long.reduceWorryPart2() = this % stressReducerModulo

}

data class Test(
    val divisor: Long,
    val IfTrueThrowTo: Long,
    val IfFalseThrowTo: Long
) {
    fun getRecipient(worryLevel: Long): Long {
       return if(worryLevel % divisor == 0L) IfTrueThrowTo else IfFalseThrowTo
    }
}

private operator fun <E> List<E>.component6(): E = this[5]