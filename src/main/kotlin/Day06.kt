fun main() {
    val input = readResource("Day6.txt").asIterable().iterator()
    //val output = findMarker(input, 4)
    val output = findMarker(input, 14)
    println("The Marker is after position $output")
}

fun findMarker(iterator: Iterator<Char>, length: Int): Int {
    val buffer = Array(length) { iterator.next() }
    var position = length
    if(buffer.uniqueElements() == length) return position

    for(element in iterator) {
        buffer[position % length] = element
        position++
        if(buffer.uniqueElements() == length) return position
    }

    return -1
}

fun<T> Array<T>.uniqueElements() = this.toSet().size
