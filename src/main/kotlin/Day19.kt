fun main() {
    val input = readResourceLines("Day19Test.txt")
//    val output = findQualitySum(input)
    val output = findGeodeProduct(input)
    println("$output")
}

//Blueprint 1: Each ore robot costs 4 ore. Each clay robot costs 2 ore. Each obsidian robot costs 3 ore and 14 clay. Each geode robot costs 2 ore and 7 obsidian.
//Blueprint 2: Each ore robot costs 2 ore. Each clay robot costs 3 ore. Each obsidian robot costs 3 ore and 8 clay. Each geode robot costs 3 ore and 12 obsidian.
private fun parseBlueprints(input: List<String>): List<Blueprint> {
    return input.map { line ->
        val (blueprint, resources) = line.split(":")
        val blueprintNumber = blueprint.split(" ")[1].trim().toInt()
        val parts = resources.split(".")
        val (oreDigits, clayDigits, obsidianDigits, geodeDigits) = parts.map { part -> part.filter { it.isDigit() || it.isWhitespace() } }.map { it.trim() }

        val oreOreCost = oreDigits.toInt()
        val clayOreCost = clayDigits.toInt()
        val (obsidianOreCost, obsidianClayCost) = obsidianDigits.split(" ").filter { it != "" }.map { it.trim().toInt() }
        val (geodeOreCost, geodeObsidianCost) = geodeDigits.split(" ").filter { it != "" }.map { it.trim().toInt() }
        Blueprint(
            blueprintNumber,
            oreOreCost,
            clayOreCost,
            obsidianOreCost,
            obsidianClayCost,
            geodeOreCost,
            geodeObsidianCost,
        )
    }
}

//remainingTime, Production -> Inventory
val cache = mutableMapOf<Pair<Int, Inventory>, Inventory>()
private fun depthFirstSearch(blueprint: Blueprint, inventory: Inventory, production: Inventory, remainingTime: Int): Int {
    if(remainingTime <= 0) return inventory.geode

    val cachedInventory = cache[remainingTime to production]
    if(cachedInventory != null && cachedInventory.containsMoreOrEqual(inventory)) return cachedInventory.geode

    cache[remainingTime to production] = inventory
    val maxGeodeCount = Operation.values().map { operation ->
        val newInventory = blueprint.buy(inventory, operation) ?: return@map inventory.geode
        val newProduction = production + operation.production

        //use old production because roboter need a turn to get ready
        depthFirstSearch(blueprint, newInventory + production, newProduction, remainingTime - 1)
    }.max()

    return maxGeodeCount
}

fun findQualitySum(input: List<String>): Int {
    val blueprints = parseBlueprints(input)
    val output = blueprints.map { blueprint ->
        val geodeCount = depthFirstSearch(blueprint, Inventory(), Inventory(ore = 1), 24).also { cache.clear() }
        println("blueprint ${blueprint.blueprintNumber}: produces $geodeCount geodes")
        blueprint.blueprintNumber * geodeCount
    }
    return output.sum()
}

fun findGeodeProduct(input: List<String>): Int {
    val blueprints = parseBlueprints(input)
    val output = blueprints.take(3).map { blueprint ->
        val geodeCount = depthFirstSearch(blueprint, Inventory(), Inventory(ore = 1), 32).also { cache.clear() }
        println("blueprint ${blueprint.blueprintNumber}: produces $geodeCount geodes")
        geodeCount
    }
    return output.reduce { a, b -> a * b }
}

data class Blueprint(
    val blueprintNumber: Int,

    val oreOreCost: Int,
    val clayOreCost: Int,
    val obsidianOreCost: Int,
    val obsidianClayCost: Int,
    val geodeOreCost: Int,
    val geodeObsidianCost: Int,
) {
    //returns null if there are not enough resources
    fun buy(inventory: Inventory, purchase: Operation): Inventory? {
        return when(purchase) {
            Operation.Ore -> {
                val newOre = inventory.ore - oreOreCost
                if(newOre < 0) null else inventory.copy(ore = newOre)
            }
            Operation.Clay -> {
                val newOre = inventory.ore - clayOreCost
                if(newOre < 0) null else inventory.copy(ore = newOre)
            }
            Operation.Obsidian -> {
                val newOre = inventory.ore - obsidianOreCost
                val newClay = inventory.clay - obsidianClayCost
                if(newOre < 0 || newClay < 0) null
                        else inventory.copy(ore = newOre, clay = newClay)
            }
            Operation.Geode -> {
                val newOre = inventory.ore - geodeOreCost
                val newObsidian = inventory.obsidian - geodeObsidianCost
                if(newOre < 0 || newObsidian < 0) null
                else inventory.copy(ore = newOre, obsidian = newObsidian)
            }
            Operation.Nothing -> inventory
        }
    }
}

data class Inventory(
    val ore: Int = 0,
    val clay: Int = 0,
    val obsidian: Int = 0,
    val geode: Int = 0,
)

private operator fun Inventory.plus(other: Inventory) = Inventory(
    this.ore + other.ore,
    this.clay + other.clay,
    this.obsidian + other.obsidian,
    this.geode + other.geode,
)

private operator fun Inventory.compareTo(other: Inventory): Int {
    if(this.ore > other.ore) return 1
    if(this.ore < other.ore) return -1

    if(this.clay > other.clay) return 1
    if(this.clay < other.clay) return -1

    if(this.obsidian > other.obsidian) return 1
    if(this.obsidian < other.obsidian) return -1

    if(this.geode > other.geode) return 1
    if(this.geode < other.geode) return -1

    return 0
}

private fun Inventory.containsMoreOrEqual(other: Inventory) =
    this.ore >= other.ore
            && this.clay >= other.clay
            && this.obsidian >= other.obsidian
            && this.geode >= other.geode

enum class Operation(val production: Inventory) {
    Ore(Inventory(ore = 1)),
    Clay(Inventory(clay = 1)),
    Obsidian(Inventory(obsidian = 1)),
    Geode(Inventory(geode = 1)),
    Nothing(Inventory()),
}
