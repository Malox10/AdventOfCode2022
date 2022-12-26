import java.util.*

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
//    return space.calculateSides()

    space.findContainedSubGraphs()
    return space.calculateSidesPart2()
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
                isLava(neighbour)
            }

            6 - neighbourCount
        }
    }

    fun calculateSidesPart2(): Int {
        return cubes.sumOf { cube ->
            val neighbourCount = CubeDirection.values().count { direction ->
                val neighbour = cube + direction.offset
                isLava(neighbour) || insideSet.contains(neighbour)
            }

            6 - neighbourCount
        }
    }

    fun isLava(cubeToCheck: Cube): Boolean {
        val hasNeighbour = inner.getOrNull( cubeToCheck.z - offset.z)?.getOrNull(cubeToCheck.y - offset.y)?.getOrNull(cubeToCheck.x - offset.x)
        return hasNeighbour ?: false
    }

    val outsideSet = mutableSetOf<Cube>()
    val insideSet = mutableSetOf<Cube>()
    fun findContainedSubGraphs() {
        inner.forEachIndexed { z, slice ->
            slice.forEachIndexed { y, line ->
                line.forEachIndexed inner@{ x, isLava ->
                    if(isLava) return@inner
                    val currentCube = Cube(z + offset.z, y + offset.y, x + offset.x) //to make debugging easier

                    if(outsideSet.contains(currentCube) || insideSet.contains(currentCube)) return@inner
                    expandSubgraph(currentCube)
                }
            }
        }
    }

    private fun expandSubgraph(cubeToExpand: Cube) {
        var subGraphTouchesOutside = cubeToExpand.isOutsideOfSpace()

        val checkedCubes = mutableListOf<Cube>()
        val cubesToCheck: Queue<Cube> = LinkedList(listOf(cubeToExpand))
        while (cubesToCheck.isNotEmpty()) {
            val cubeToCheck = cubesToCheck.remove()
            if(checkedCubes.contains(cubeToCheck)) continue

            CubeDirection.values().forEach { direction ->
                val potentialNeighbour = direction.offset + cubeToCheck
                if(checkedCubes.contains(potentialNeighbour) || cubesToCheck.contains(potentialNeighbour)) return@forEach

                if(isLava(potentialNeighbour)) return@forEach
                if(potentialNeighbour.isOutsideOfSpace()) {
                    subGraphTouchesOutside = true
                    return@forEach
                }
                //add air cube, that is inside the space and hasn't been checked before
                cubesToCheck.add(potentialNeighbour)
                checkedCubes.add(cubeToCheck)
            }
        }

        val outputSet = if(subGraphTouchesOutside) outsideSet else insideSet
        outputSet.add(cubeToExpand)
        outputSet.addAll(checkedCubes)
    }

    private fun Cube.isOutsideOfSpace(): Boolean {
        val output = inner.getOrNull(this.z - offset.z)?.getOrNull(this.y - offset.y)?.getOrNull(this.x - offset.x)
        return output == null
    }

    private fun Cube.isOnOutsideEdge(): Boolean {
        if(this.x == 1 || this.y == 1 || this.z == 1) return true
        if(this.z == inner.size) return true
        if(this.y == inner.first().size) return true
        if(this.x == inner.first().first().size) return true
        return false
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