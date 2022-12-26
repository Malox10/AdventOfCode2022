fun main() {
    val input = readResourceLines("Day18.txt")
    val output = findAmountOfCubeSides(input)
    println("The amount of visible sides is: $output")
}

private fun parseCubes(input: List<String>): Set<Cube> {
    val set = input.map { line ->
        val (z, y, x) = line.split(",").map { it.trim().toInt() }
        Cube(z, y, x)
    }.toSet()

    return set
}

fun findAmountOfCubeSides(input: List<String>): Int {
    val cubes = parseCubes(input)
    val minX = cubes.minOf { it.x }
    val minY = cubes.minOf { it.y }
    val minZ = cubes.minOf { it.z }

    val maxX = cubes.maxOf { it.x }
    val maxY = cubes.maxOf { it.y }
    val maxZ = cubes.maxOf { it.z }

    val dimensions = Cube(maxZ - minZ + 1, maxY - minY + 1, maxX - minX + 1)
    val offset = Cube(minZ, minY, minX)
    val space = Space(cubes, offset, dimensions)
    return space.calculateSides()
}

private class Space(
    private val cubes: Set<Cube>,
    private val offset: Cube,
    private val dimensions: Cube,
) {
    val inner: List<List<MutableList<Boolean>>> = List(dimensions.z) { List (dimensions.y) { MutableList ( dimensions.x) { false } } }

    init {
        populateCubes()
    }

    fun populateCubes() {
        cubes.forEach { cube ->
            val newCube = cube - offset
            inner[newCube.z][newCube.y][newCube.x] = true
        }
    }

    fun calculateSides(): Int {
        return cubes.sumOf { cube ->
            val neighbourCount = CubeDirection.values().count { direction ->
                val neighbour = cube + direction.offset
                val hasNeighbour = inner.getOrNull(neighbour.z - offset.z)?.getOrNull(neighbour.y - offset.y)?.getOrNull(neighbour.x - offset.x)
                hasNeighbour ?: false
            }

            6 - neighbourCount
        }
    }
}


private data class Cube(val z: Int, val y: Int, val x: Int)

private enum class CubeDirection(val offset: Cube) {
    Front(Cube(-1, 0, 0)),
    Back(Cube(1, 0, 0)),
    Left(Cube(0, 0, -1)),
    Right(Cube(0, 0, 1)),
    Up(Cube(0, 1, 0)),
    Down(Cube(0, -1, 0)),
}

private operator fun Cube.plus(other: Cube) = Cube(this.z + other.z, this.y + other.y, this.x + other.x)
private operator fun Cube.minus(other: Cube) = Cube(this.z - other.z, this.y - other.y, this.x - other.x)