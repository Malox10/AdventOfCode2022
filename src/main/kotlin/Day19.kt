import Robot.*
import kotlin.math.ceil

fun main() {
    val input = readResourceLines("Day19Test.txt")
    val output = findQualitySum(input)
//    val output = findGeodeProduct(input)
    println("$output")
}

//Blueprint 1: Each ore robot costs 4 ore. Each clay robot costs 2 ore. Each obsidian robot costs 3 ore and 14 clay. Each geode robot costs 2 ore and 7 obsidian.
//Blueprint 2: Each ore robot costs 2 ore. Each clay robot costs 3 ore. Each obsidian robot costs 3 ore and 8 clay. Each geode robot costs 3 ore and 12 obsidian.
private fun parseBlueprints(input: List<String>): List<Blueprints> {
    return input.map { line ->
        val (blueprint, resources) = line.split(":")
        val blueprintNumber = blueprint.split(" ")[1].trim().toInt()
        val parts = resources.split(".")
        val (oreDigits, clayDigits, obsidianDigits, geodeDigits) = parts.map { part -> part.filter { it.isDigit() || it.isWhitespace() } }.map { it.trim() }

        val oreOreCost = oreDigits.toInt()
        val clayOreCost = clayDigits.toInt()
        val (obsidianOreCost, obsidianClayCost) = obsidianDigits.split(" ").filter { it != "" }.map { it.trim().toInt() }
        val (geodeOreCost, geodeObsidianCost) = geodeDigits.split(" ").filter { it != "" }.map { it.trim().toInt() }
        Blueprints(
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


private fun depthFirstViaNodes(blueprint: Blueprints, inventory: Inventory, production: Inventory, remainingTime: Int): Int {
    if(remainingTime <= 0) return inventory.geode

    val cachedInventory = caches[remainingTime to production]
    if(cachedInventory != null && cachedInventory.containsMoreOrEqual(inventory)) return cachedInventory.geode

    caches[remainingTime to production] = inventory

    values().map { robot ->
        val (cycles, newInventory) = blueprint.cyclesToBuyRobot(inventory, production, robot)
        val newRemainingTime = remainingTime - cycles
        if(newRemainingTime < 0) return inventory.geode + remainingTime * production.geode
        if(newRemainingTime == 0) return 42
    }

    TODO()
}


///Make a Node search, where we don't get each state but instead get each state with a Purchase

//remainingTime, Production -> Inventory
val caches = mutableMapOf<Pair<Int, Inventory>, Inventory>()
private fun depthFirstSearch(blueprint: Blueprints, inventory: Inventory, production: Inventory, remainingTime: Int): Int {
    if(remainingTime <= 0) return inventory.geode

    val cachedInventory = caches[remainingTime to production]
    if(cachedInventory != null && cachedInventory.containsMoreOrEqual(inventory)) return cachedInventory.geode

    caches[remainingTime to production] = inventory

    val pendingInventories = mutableListOf(inventory to production)
    val handledInventory = mutableSetOf<Pair<Inventory, Inventory>>()
    while (pendingInventories.isNotEmpty()) {
        val (currentInventory, currentProduction) = pendingInventories.removeFirst()
        Operation.values().map { operation ->
            val newInventory = blueprint.buy(currentInventory, operation)
            //this produces adding multiple of the same pair a lot of times
            if(newInventory == null || operation == Operation.Nothing) {
                handledInventory.add(currentInventory to currentProduction)
                return@map
            }
            val newProduction = production + operation.production
            pendingInventories.add(newInventory to newProduction)
        }
    }

    //use old production because roboter need a turn to get ready
    return handledInventory.maxOf { (newInventory, newProduction) ->
        depthFirstSearch(blueprint, newInventory + production, newProduction, remainingTime - 1)
    }
}

fun findQualitySum(input: List<String>): Int {
    val blueprints = parseBlueprints(input)
    val output = blueprints.map { blueprint ->
        val geodeCount = depthFirstSearch(blueprint, Inventory(), Inventory(ore = 1), 18).also { caches.clear() }
        println("blueprint ${blueprint.blueprintNumber}: produces $geodeCount geodes")
        blueprint.blueprintNumber * geodeCount
    }
    return output.sum()
}

fun findGeodeProduct(input: List<String>): Int {
    val blueprints = parseBlueprints(input)
    val output = blueprints.take(3).map { blueprint ->
        val geodeCount = depthFirstSearch(blueprint, Inventory(), Inventory(ore = 1), 32).also { caches.clear() }
        println("blueprint ${blueprint.blueprintNumber}: produces $geodeCount geodes")
        geodeCount
    }
    return output.reduce { a, b -> a * b }
}

private data class Blueprints(
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
            Operation.BuyOre -> {
                val newOre = inventory.ore - oreOreCost
                if(newOre < 0) null else inventory.copy(ore = newOre)
            }
            Operation.BuyClay -> {
                val newOre = inventory.ore - clayOreCost
                if(newOre < 0) null else inventory.copy(ore = newOre)
            }
            Operation.BuyObsidian -> {
                val newOre = inventory.ore - obsidianOreCost
                val newClay = inventory.clay - obsidianClayCost
                if(newOre < 0 || newClay < 0) null
                        else inventory.copy(ore = newOre, clay = newClay)
            }
            Operation.BuyGeode -> {
                val newOre = inventory.ore - geodeOreCost
                val newObsidian = inventory.obsidian - geodeObsidianCost
                if(newOre < 0 || newObsidian < 0) null
                else inventory.copy(ore = newOre, obsidian = newObsidian)
            }
            Operation.Nothing -> inventory
        }
    }


    //should've made maps where the keys are the type of material, instead of this mess
    fun cyclesToBuyRobot(inventory: Inventory, production: Inventory, robotToBuy: Robot): Pair<Int, Inventory> {
        when (robotToBuy) {
            Ore -> {
                val (cycles, newIronAmount) = calculateCyclesAndNewStock(inventory.ore, production.ore, this.oreOreCost)
                return cycles to inventory.copy(ore = newIronAmount)
            }
            Clay -> {
                val (cycles, newIronAmount) = calculateCyclesAndNewStock(inventory.ore, production.ore, this.clayOreCost)
                return cycles to inventory.copy(ore = newIronAmount)
            }
            Obsidian -> {
                val (ironCycles, newIronAmount) = calculateCyclesAndNewStock(inventory.ore, production.ore, this.obsidianOreCost)
                val (clayCycles, newClayAmount) = calculateCyclesAndNewStock(inventory.clay, production.clay, this.obsidianClayCost)

                val delta = clayCycles - ironCycles
                return if(delta == 0) ironCycles to inventory.copy(ore = newIronAmount, clay = newClayAmount)
                else if(delta > 0) {
                    val ironAmount = newIronAmount + (delta * production.ore)
                    clayCycles to inventory.copy(ore = ironAmount, clay = newClayAmount)
                } else {
                    val clayAmount = newClayAmount + (delta * production.clay)
                    ironCycles to inventory.copy(ore = newIronAmount, clay = clayAmount)
                }
            }
            Geode -> {
                val (ironCycles, newIronAmount) = calculateCyclesAndNewStock(inventory.ore, production.ore, this.geodeOreCost)
                val (obsidianCycles, newObsidianAmount) = calculateCyclesAndNewStock(inventory.obsidian, production.obsidian, this.geodeObsidianCost)

                val delta = obsidianCycles - ironCycles
                return if(delta == 0) ironCycles to inventory.copy(ore = newIronAmount, obsidian = newObsidianAmount)
                else if(delta > 0) {
                    val ironAmount = newIronAmount + (delta * production.ore)
                    obsidianCycles to inventory.copy(ore = ironAmount, obsidian = newObsidianAmount)
                } else {
                    val obsidianAmount = newObsidianAmount + (delta * production.obsidian)
                    ironCycles to inventory.copy(ore = newIronAmount, obsidian = obsidianAmount)
                }
            }
        }
    }
}

fun calculateCyclesAndNewStock(currentStock: Int, production: Int, cost: Int): Pair<Int, Int> {
    val delta = cost - currentStock
    if(delta <= 0) return 0 to -delta
    val cycle = ceil(delta.toDouble() / production).toInt()
    val producedAmount = cycle * production

    return cycle to producedAmount - delta
}

data class Inventory(
    val ore: Int = 0,
    val clay: Int = 0,
    val obsidian: Int = 0,
    val geode: Int = 0,
) {
    operator fun plus(other: Inventory) = Inventory(
        this.ore + other.ore,
        this.clay + other.clay,
        this.obsidian + other.obsidian,
        this.geode + other.geode,
    )

    fun containsMoreOrEqual(other: Inventory) =
        this.ore >= other.ore
                && this.clay >= other.clay
                && this.obsidian >= other.obsidian
                && this.geode >= other.geode

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
}

enum class Operation(val production: Inventory) {
    BuyOre(Inventory(ore = 1)),
    BuyClay(Inventory(clay = 1)),
    BuyObsidian(Inventory(obsidian = 1)),
    BuyGeode(Inventory(geode = 1)),
    Nothing(Inventory()),
}

private enum class Robot(val production: Inventory) {
    Ore(Inventory(ore = 1)),
    Clay(Inventory(clay = 1)),
    Obsidian(Inventory(obsidian = 1)),
    Geode(Inventory(geode = 1)),
}