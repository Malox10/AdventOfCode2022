import java.lang.Integer.max
import kotlin.math.ceil

fun main() {
    val input = readResourceLines("Day19.txt")
//    val output = findQualitySum(input)
    val output = findAmountOfGeodes(input)
    println("$output")
}

fun findAmountOfGeodes(input: List<String>): Int {
    val blueprints = parseBlueprints(input)
    val maxGeodes = blueprints.take(3).map { blueprint ->
        val maxGeodes = depthFirstWithSteps(blueprint, Stock(), Stock(ore = 1), 32, setOf(Material.Ore, Material.Clay)).also { DFSCache.clear() }
        DFSCache.clear()
        println("blueprint ${blueprint.number}: produces $maxGeodes geodes")
        maxGeodes
    }

    return maxGeodes.reduce { a, b -> a * b }
//    return maxGeodes.sum()
}

//private val DFSCache = mutableMapOf<Pair<Stock, Int>, Stock>()
//private fun depthFirstWithSteps(blueprint: Blueprint, inventory: Stock, production: Stock, time: Int): Int {
//    if(time <= 0) return inventory.geode
//
//    val cachedInventory = DFSCache[production to time]
//    if(cachedInventory != null && cachedInventory.containsMoreOrEqual(inventory)) return cachedInventory.geode
//
//    DFSCache[production to time] = inventory
//    val maxGeodes = blueprint.robots.entries.mapNotNull { (robotType, materialCosts) ->
//        val cycleLength = materialCosts.positiveValues().maxOf inner@{ (material, amount) ->
//            val currentStock = inventory.getStockOf(material)
//            if(currentStock >= amount) return@inner 0
//
//            val currentProduction = production.getStockOf(material)
//            if(currentProduction == 0) return@mapNotNull null
//            val delta = amount - currentStock
//            val cycles = ceil(delta.toDouble() / currentProduction).toInt()
//
//            cycles
//        }
//
//        val remainingTime = time - cycleLength
//        if(remainingTime <= 0 ) return inventory.geode + production.geode * time
//
//        val newInventory = inventory.update(production, cycleLength, materialCosts)
//        val newProduction = robotProduction[robotType]!! + production
//        depthFirstWithSteps(blueprint, newInventory, newProduction, remainingTime)
//    }.max()
//
//    return maxGeodes
//}

private val runningDFSCache = mutableMapOf<Stock, Pair<Int, Stock>>()
private val DFSCache = mutableMapOf<Pair<Stock, Int>, Stock>()
private fun depthFirstWithSteps(blueprint: Blueprint, inventory: Stock, production: Stock, time: Int, availableRobots: Set<Material>): Int {
    if(time <= 0) return inventory.geode

    val cachedInventory = DFSCache[production to time]
    if(cachedInventory != null && cachedInventory.containsMoreOrEqual(inventory)) return cachedInventory.geode

//    val (remainingTimeOfCache, runningCache) = runningDFSCache[production]!!
//    if(remainingTimeOfCache >= time && runningCache.containsMoreOrEqual())

    val maxGeodes = availableRobots.mapNotNull {  robotType->
        val materialCosts = blueprint.robots[robotType]!!
        val cycleLength = materialCosts.positiveValues().maxOf { (material, amount) ->
            val currentStock = inventory.getStockOf(material)
            if(currentStock >= amount) return@maxOf 1

            val currentProduction = production.getStockOf(material)
            if(currentProduction == 0) return@mapNotNull null
            val delta = amount - currentStock
            val cycles = ceil(delta.toDouble() / currentProduction).toInt()

            cycles + 1
        }

        val remainingTime = time - cycleLength
        if(remainingTime <= 0 ) return@mapNotNull inventory.geode + production.geode * time

        val newInventory = inventory.update(production, cycleLength, materialCosts)
        val newProduction = robotProduction[robotType]!! + production
        val newAvailableRobots =

        //
//        else if(newProduction.ore >= blueprint.maxIronPerCycle && availableRobots.size == 4) { availableRobots - Material.Ore }
//        else {
            when (robotType) {
                Material.Clay -> availableRobots + Material.Obsidian
                Material.Obsidian -> availableRobots + Material.Geode
                Material.Ore -> {
                    if(newProduction.ore >= blueprint.maxIronPerCycle) availableRobots - Material.Ore else availableRobots
                }
                else -> availableRobots
            }
//        }

        val maxGeodes = depthFirstWithSteps(blueprint, newInventory, newProduction, remainingTime, newAvailableRobots)
        DFSCache[newProduction to remainingTime] = newInventory
        maxGeodes
    }.max()

    return maxGeodes
}

///adds the amount times the production to current stock
private fun Stock.update(production: Stock, amount: Int, cost: Stock): Stock {
    return Stock(
        this.ore + production.ore * amount - cost.ore,
        this.clay + production.clay * amount - cost.clay,
        this.obsidian + production.obsidian * amount - cost.obsidian,
        this.geode + production.geode * amount - cost.geode
    )
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
            maxOf(oreOreCost, clayOreCost, obsidianOreCost, geodeOreCost),
            mapOf (
                Material.Geode to Stock(ore = geodeOreCost, obsidian = geodeObsidianCost),
                Material.Obsidian to Stock(ore = obsidianOreCost, clay = obsidianClayCost),
                Material.Clay to Stock(ore = clayOreCost),
                Material.Ore to Stock(ore = oreOreCost)
            )
        )
    }
}

private enum class Material {
    Ore,
    Clay,
    Obsidian,
    Geode
}

private val robotProduction = mapOf(
    Material.Ore to Stock(ore = 1),
    Material.Clay to Stock(clay = 1),
    Material.Obsidian to Stock(obsidian = 1),
    Material.Geode to Stock(geode = 1),
)

private typealias MaterialCost = Pair<Material, Int>

private data class Blueprint(
    val number: Int,
    val maxIronPerCycle: Int,
    val robots: Map<Material, Stock>
)

private data class Stock(
    val ore: Int = 0,
    val clay: Int = 0,
    val obsidian: Int = 0,
    val geode: Int = 0,
) {
    fun getStockOf(material: Material): Int {
        return when (material) {
            Material.Ore -> this.ore
            Material.Clay -> this.clay
            Material.Obsidian -> this.obsidian
            Material.Geode -> this.geode
        }
    }

    fun positiveValues(): List<MaterialCost> {
        val list = mutableListOf<MaterialCost>()
        if(ore > 0) list.add(Material.Ore to ore)
        if(clay > 0) list.add(Material.Clay to clay)
        if(obsidian > 0) list.add(Material.Obsidian to obsidian)
        if(geode > 0) list.add(Material.Geode to geode)

        return list
    }

    fun containsMoreOrEqual(other: Stock) =
        this.ore >= other.ore
                && this.clay >= other.clay
                && this.obsidian >= other.obsidian
                && this.geode >= other.geode
}

private operator fun Stock.plus(other: Stock) = Stock(
    this.ore + other.ore,
    this.clay + other.clay,
    this.obsidian + other.obsidian,
    this.geode + other.geode
)
