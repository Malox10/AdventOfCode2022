import java.util.*

fun main() {
    val input = readResourceLines("Day7.txt")
//    val output = findDirectories(input)
    val output = findDirectoryToDelete(input)
    println("$output")
}

fun findDirectoryToDelete(input: List<String>): Long {
    val tree = DoubleLinkedTree(input, Node(Type.Directory, "/", null))
    val directorySizes = tree.getSetOfDirectories().map { it.getDirectorySize() }
    val remainingSpace = 70000000 - directorySizes.max()

    return directorySizes.map { remainingSpace + it }.filter { it >= 30000000 }.minOf { it } - remainingSpace
}

@Suppress("unused")
fun findDirectories(input: List<String>): Long {
    val tree = DoubleLinkedTree(input, Node(Type.Directory, "/", null))

    val directories = tree.getSetOfDirectories()
    return directories.map { it.getDirectorySize() }.filter { it <= 100000 }.sum()
}

data class Node(
    val type: Type,
    val name: String,
    val parent: Node?, //null if at root
    val size: Long = 0,
    val children: MutableList<Node> = mutableListOf(),
) {
    override fun toString(): String {
        return name
    }

    override fun hashCode(): Int {
        return Objects.hash(this.getPath())
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Node) return false
        return this.getPath() == other.getPath()
    }

    fun getPath(): String {
        var path = "/"
        var currentLocation = this
        while(currentLocation.parent != null) {
            path = "/${currentLocation.name}$path"
            currentLocation = currentLocation.parent!!
        }

        return path
    }

    fun getDirectorySize(): Long { //Memoize for performance
        if (this.type == Type.File) return this.size
        return this.children.sumOf { node ->
            node.getDirectorySize()
        }
    }
}

enum class Type {
    Directory,
    File,
}

class DoubleLinkedTree(input: List<String>, private val rootNode: Node) {
    private var currentLocation = rootNode

    init {
        val list = input.drop(1)

        list.forEach { line ->
            val parts = line.split(" ")
            when {
                line.startsWith("$") -> {
                    if(parts.size == 2) return@forEach //$ ls

                    val (_, _, parameter) = parts
                    if(parameter == "..") { //$ cd ..
                        moveUpDirectory()
                    } else { //$ cd [dirName], $ cd fd
                        moveToDirectory(parameter)
                    }
                }
                line.startsWith("dir") -> { //dir [dirName], dir fd
                    val (_, directoryName) = parts
                    createSubDirectory(directoryName)
                }
                else -> { //[fileSize] [fileName], 238232 f.txt
                    val (size, fileName) = parts
                    createFile(fileName, size.toLong())
                }
            }
        }
    }

    private fun moveUpDirectory() {
        currentLocation = currentLocation.parent ?: error("Can't navigate to parent of root")
    }

    private fun moveToDirectory(directoryName: String) {
        if(!doesSubDirectoryExist(directoryName)) {
            println("Tried to navigate into Subdirectory that didn't exist. Creating directory $directoryName")
            createSubDirectory(directoryName)
        }
        currentLocation = currentLocation.children.find { it.name == directoryName }!!
    }

    private fun doesSubDirectoryExist(directoryName: String) =
        currentLocation.children.find { it.name == directoryName } != null

    private fun createSubDirectory(directoryName: String) {
        val newDirectory = Node(Type.Directory, directoryName, currentLocation)
        currentLocation.children.add(newDirectory)
    }

    private fun createFile(fileName: String, size: Long) {
        val newFile = Node(Type.File, fileName, currentLocation, size)
        currentLocation.children.add(newFile)
    }

    fun getSetOfDirectories(node: Node = rootNode): Set<Node> {
        if(node.type == Type.File) return setOf(node)
        val children = node.children.flatMap {childNode ->
            getSetOfDirectories(childNode)
        }.toSet().filter { it.type == Type.Directory }
        return setOf(node) + children
    }
}
