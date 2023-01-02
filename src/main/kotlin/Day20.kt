fun main() {
    val input = readResourceLines("Day20.txt")
    val output = findCoordinateSum(input)
    println("The coordinate sum is: $output")
}

private fun parseInput(input: List<String>) = input.map { it.trim().toLong() }

private fun findCoordinateSum(input: List<String>): Long {
    val numbers = parseInput(input)
    val circularList = MyCircularList(numbers)
    circularList.mixEntireList()
    return circularList.grooveCoordinates()
}
data class LinkedNode<T>(
    val inner: T,
    var previous: LinkedNode<T>? = null,
    var successor: LinkedNode<T>? = null,
) {
    override fun toString() = inner.toString()
}

private class MyCircularList(val list: List<Long>, val isPartTwo: Boolean = true) {
    val head: LinkedNode<Long>
    val map: Map<Long, LinkedNode<Long>>

    val size get() = map.size
    var zero: LinkedNode<Long>? = null

    init {
        val nodes = list.map { LinkedNode(it * if(isPartTwo) 811589153 else 1 )}

        val firstNode = nodes.first()
        val lastNode = nodes.last()

        val circularNodes = listOf(lastNode) + nodes + firstNode
        circularNodes.windowed(3).forEach { (previous, inner, successor) ->
            if(inner.inner == 0L) zero = inner
            inner.previous = previous
            inner.successor = successor
        }

        head = nodes.first()
        map = nodes.mapIndexed { index, linkedNode -> index.toLong() to linkedNode }.toMap()
    }

    fun insertAfter(previousNode: LinkedNode<Long>, nodeToInsert: LinkedNode<Long>) {
        val successorNode = previousNode.successor

        previousNode.successor = nodeToInsert
        successorNode!!.previous = nodeToInsert

        nodeToInsert.previous = previousNode
        nodeToInsert.successor = successorNode
    }

    fun removeNode(nodeToRemove: LinkedNode<Long>) {
        val previous = nodeToRemove.previous
        val successor = nodeToRemove.successor

        previous!!.successor = successor
        successor!!.previous = previous

        nodeToRemove.previous = null
//        nodeToRemove.successor = null
    }

    fun mix(index: Long) {
        val node = map[index]!!
        //to lazy to solve this cast
        val stepsToMove = node.inner.mod(this.size - 1)
        if(stepsToMove == 0) return
        removeNode(node)

        var currentSuccessor = node
        repeat(stepsToMove) {
            currentSuccessor = currentSuccessor.successor!!
        }

        insertAfter(currentSuccessor, node)
    }

    fun mixEntireList() {
        repeat(if(isPartTwo) 10 else 1) {
            list.forEachIndexed { index, _ ->
                mix(index.toLong())
//                printList()
            }
        }
    }

    fun grooveCoordinates(): Long {
        val offsets = listOf(1000, 2000, 3000).map { it.mod(this.size) }
        return offsets.map { offset ->
            var currentNode = zero!!
            repeat(offset) {
                currentNode = currentNode.successor!!
            }

            currentNode
        }.sumOf { it.inner as Long }
    }

    fun printList() {
        var currentNode = head
        print("[$head")
        repeat(size - 1) {
            currentNode = currentNode.successor!!
            print(", $currentNode")
        }
        println("]")
    }
}
