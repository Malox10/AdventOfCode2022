fun main() {
    val input = readResourceLines("Day13.txt")
    val output = findNumberOfMatchingPacketPairs(input)
    println("$output")
}

typealias PacketPairs = Pair<Packet.Lists, Packet.Lists>
fun findNumberOfMatchingPacketPairs(input: List<String>): Int {
    val unparsedPairs = input.chunked(3).map { it[0] to it[1] }
    val packetPairs = unparsedPairs.map { pair ->
        val (first, second) = pair.toList().map {
            val iterator = it.toTokens().iterator()
            iterator.next() //skip the first item as it is always bracket open
            parsePacketList(iterator)
        }

        first to second
    }

    val states = packetPairs.map { comparePackets(it) }
    val output = states.mapIndexedNotNull { index, state ->
        if( state == State.InOrder) index + 1 else null
    }.sum()
    return output
}


fun comparePackets(pairs: PacketPairs): State {
    val (first, second) = pairs
    val firstLength = first.value.size
    val secondLength = second.value.size

    val (smallerSize, state) =
        if(firstLength < secondLength) firstLength to State.InOrder
        else if(secondLength < firstLength) secondLength to State.OutOfOrder
        else firstLength to State.Continue

    for(i in 0 until smallerSize) {
        val firstElement = first.value[i]
        val secondElement = second.value[i]

        val itemState = if(firstElement is Packet.Ints && secondElement is Packet.Ints) {
            compareInts(firstElement.value, secondElement.value)
        } else {
            val firstList = when(firstElement) {
                is Packet.Lists -> firstElement
                is Packet.Ints -> Packet.Lists(listOf(firstElement))
            }

            val secondList = when(secondElement) {
                is Packet.Lists -> secondElement
                is Packet.Ints -> Packet.Lists(listOf(secondElement))
            }

            comparePackets(firstList to secondList)
        }

        when (itemState) {
            State.InOrder -> return State.InOrder
            State.Continue -> {}
            State.OutOfOrder -> return State.OutOfOrder
        }
    }

    return state
}

fun compareInts(first: Int, second: Int): State {
    return if(first < second) State.InOrder
    else if(first > second) State.OutOfOrder
    else State.Continue
}

fun parsePacketList(tokens: Iterator<Token>): Packet.Lists {
    val currentList = mutableListOf<Packet>()
    do {
        when (val nextToken = tokens.next()) {
            Token.Bracket(true) -> currentList.add(parsePacketList(tokens))
            is Token.Bracket -> break
            is Token.Integer -> currentList.add(Packet.Ints(nextToken.value))
            is Token.Comma -> {}
        }
    } while(tokens.hasNext())

    return Packet.Lists(currentList)
}

const val separator = "#"
fun String.toTokens(): List<Token> {
    val newString = this.replace(",", "$separator,$separator" )
    .replace("[", "$separator[$separator")
    .replace("]", "$separator]$separator")

    val tokenStrings = newString.split(separator).filter { it != "" }
    return tokenStrings.map {
        when (it) {
            "[" -> Token.Bracket(true)
            "]" -> Token.Bracket(false)
            "," -> Token.Comma
            else -> Token.Integer(it.toInt())
        }
    }
}

sealed class Token{
    data class Bracket(val isOpen: Boolean) : Token()
    object Comma : Token()
    data class Integer(val value: Int) : Token()
}

enum class State {
    OutOfOrder, Continue, InOrder
}

sealed class Packet {
    class Lists(val value: List<Packet>) : Packet()
    class Ints(val value: Int) : Packet()
}