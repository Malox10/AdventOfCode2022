fun main() {
    val input = readResourceLines("Day21.txt")
//    val output = findMonkeyValue(input)
//    println("The monkey named root will yell: $output")
    val output = findNumberToYell(input)
    println("You need to yell $output")
}

private fun parseMonkeys(input: List<String>): Map<String, MonkeyOperation> {
    return input.associate { line ->
        val (name, operationString) = line.split(":").map { it.trim() }
        val number = operationString.toLongOrNull()
        if(number != null) return@associate name to MonkeyOperation.MonkeyNumber(number)

        val (firstMonkey, operator, secondMonkey) = operationString.split(" ").map { it.trim() }
        val operation = when (operator) {
            "+" -> { {a: Long, b: Long -> a + b} to OperationType.Plus }
            "-" -> { {a: Long, b: Long -> a - b} to OperationType.Minus }
            "*" -> { {a: Long, b: Long -> a * b} to OperationType.Times }
            "/" -> { {a: Long, b: Long -> a / b} to OperationType.Divide }
            else -> error("Can't parse $operator")
        }
        name to MonkeyOperation.MonkeyMath(firstMonkey, secondMonkey, operation.second, operation.first)
    }
}

private fun parseReverseMonkey(input: List<String>): Map<String, MonkeyOperation> {
    return input.associate { line ->
        val (name, operationString) = line.split(":").map { it.trim() }
        val number = operationString.toLongOrNull()
        if(number != null) return@associate name to MonkeyOperation.MonkeyNumber(number)

        val (firstMonkey, operator, secondMonkey) = operationString.split(" ").map { it.trim() }
        val operation = when (operator) {
            //h = a + b
            //a = h - b
            //b = h - a
            "+" -> { {h: Long, b: Long -> h - b} to OperationType.Plus }
            //h = a - b
            //a = h + b
            //b = a - h
            "-" -> { {h: Long, b: Long -> h + b} to OperationType.Minus }
            //h = a * b
            //a = h / b
            //b = h / a
            "*" -> { {h: Long, b: Long -> h / b} to OperationType.Times }
            //h = a / b
            //a = h * b
            //b = a / h
            "/" -> { {h: Long, b: Long -> h * b} to OperationType.Divide }
            else -> error("Can't parse $operator")
        }
        name to MonkeyOperation.MonkeyMath(firstMonkey, secondMonkey, operation.second, operation.first)
    }
}

fun findMonkeyValue(input: List<String>): Long {
    val operations = parseMonkeys(input)

    return operations.findNumberOf("root", false)!!
}

fun findNumberToYell(input: List<String>): Long {
    val operations = parseMonkeys(input)
    val reverseOperations = parseReverseMonkey(input)

    val monkey = operations["root"] as MonkeyOperation.MonkeyMath
    val leftSide = operations.findNumberOf(monkey.firstMonkey) to monkey.firstMonkey
    val rightSide = operations.findNumberOf(monkey.secondMonkey) to monkey.secondMonkey

    val keepOrder = leftSide.first == null
    val (humanSide, otherSide) = if(keepOrder) leftSide to rightSide else rightSide to leftSide

    return operations.findReverseNumberOf(otherSide.first!!, humanSide.second, operations[humanSide.second] as MonkeyOperation.MonkeyMath, reverseOperations)
}

private fun Map<String, MonkeyOperation>.findReverseNumberOf(target: Long, name: String, monkey: MonkeyOperation.MonkeyMath, reverseOperations: Map<String, MonkeyOperation>): Long {
    val leftSide = this.findNumberOf(monkey.firstMonkey) to monkey.firstMonkey
    val rightSide = this.findNumberOf(monkey.secondMonkey) to monkey.secondMonkey

    val keepSide = leftSide.first == null
    val (humanSide, otherSide) = if(keepSide) leftSide to rightSide else rightSide to leftSide

    val math = reverseOperations[name]!! as MonkeyOperation.MonkeyMath

    val newTarget = if(keepSide) math.operation(target, otherSide.first!!) else {
        when (math.operationType) {
            //b = a - h
            OperationType.Minus -> { otherSide.first!! - target }
            //b = a / h
            OperationType.Divide -> { otherSide.first!! / target }
            else -> math.operation(target, otherSide.first!!)
        }
    }
    if(humanSide.second == "humn") return newTarget
    return this.findReverseNumberOf(newTarget, humanSide.second, this[humanSide.second]!! as MonkeyOperation.MonkeyMath, reverseOperations)
}


private val cache = mutableMapOf<String, Long>()
private fun Map<String, MonkeyOperation>.findNumberOf(name: String, isPartTwo: Boolean = true): Long? {
    val cachedValue = cache[name]
    if(cachedValue != null) return cachedValue
    if(isPartTwo && name == "humn") return null
    return when(val monkey = this[name]!!) {
        is MonkeyOperation.MonkeyNumber -> monkey.number
        is MonkeyOperation.MonkeyMath -> {
            val first = this.findNumberOf(monkey.firstMonkey, isPartTwo) ?: return null
            val second = this.findNumberOf(monkey.secondMonkey, isPartTwo) ?: return null

            val result = monkey.operation(first, second)
            cache[name] = result
            result
        }
    }
}

private sealed class MonkeyOperation {
    data class MonkeyNumber(val number: Long) : MonkeyOperation()
    data class MonkeyMath(
        val firstMonkey: String,
        val secondMonkey: String,
        val operationType: OperationType,
        val operation: (Long, Long) -> Long,
    ) : MonkeyOperation()
}

private enum class OperationType {
    Plus(),
    Minus(),
    Times(),
    Divide(),
}
