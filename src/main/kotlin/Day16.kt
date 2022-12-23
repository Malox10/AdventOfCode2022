import java.util.*
import kotlin.system.measureTimeMillis

fun main() {
    val input = readResourceLines("Day16.txt")
//    val output = findMaxPressure(input)
    val output = part2(input)
    println("The maximum pressure released is: $output")
}

//too low 1863
//too high 1900
//too high 1971

const val startingNodeName = "AA"
private fun parseInput(input: List<String>): List<Valve> {
    val valveNodes = input.map { it.replace("Valve","",true) }
        .map {
            val (valveString, tunnelString) = it.split(";")
            val (valveName, valveFlow) = parseValve(valveString)
            val tunnels = parseTunnels(tunnelString)
            ValveNode(valveName, valveFlow, tunnels.toSet())
        }

    val allValvesMap = valveNodes.associateBy { it.name }
    val endValvesMap = valveNodes.filter { it.flowRate > 0 || it.name == startingNodeName }.associateBy { it.name }

    val shortestPath = endValvesMap.map { (name, node) ->
        val targets = endValvesMap.filter { it.value.name != name }.map { it.value }

        targets.map { target ->
            node to node.findShortestPathTo(target.name, allValvesMap)
        }
    }

    shortestPath.forEach { start ->
        start.forEach { println("from ${it.first.name} to ${it.second.first} in ${it.second.second}") }
    }

    val valveToDestination = shortestPath.map { list ->
        val origin = list.first().first
        val destinationMap= list.associate { (_, destinationAndCost) ->
            val (destinationName, cost) = destinationAndCost
            val value = endValvesMap[destinationName]!! to cost

            destinationName to value
        }


        Valve(origin.name, origin.flowRate, null) to destinationMap
    }

    val valves = valveToDestination.map { it.first }

    val newDestinations = valveToDestination.associate { (origin, destination) ->
        val newDestinations = destination.entries.associate { (_, value) ->
            val (valveNode, cost) = value
            val valve = valves.find { it.name == valveNode.name }!!
            val newValue = valve to cost

            val value = valve.name to newValue
            value
        }
        origin to newDestinations
    }

    //assign the map to each valve mapping name, to the next node and the associated cost of traveling there and activating the node
    valves.forEach { valve -> valve.travelTimes = newDestinations[valve]!! }

    return valves
}

private fun ValveNode.findShortestPathTo(target: String, allValves: Map<String, ValveNode>): Pair<String, Int> {
    var pathLength = 2
    if(this.connections.contains(target)) return target to pathLength

    val visitedNodes = this.connections.toMutableSet()
    var currentNodes = this.connections.map { allValves[it]!! }.toMutableSet()

    while(true) {
        val newNodeSet = mutableSetOf<ValveNode>()
        pathLength++
        currentNodes.forEach { node ->
            val newNodes = node.connections.subtract(visitedNodes)
            if(newNodes.contains(target)) return target to pathLength
            newNodeSet += newNodes.map { allValves[it]!! }
        }

        visitedNodes += newNodeSet.map { it.name }
        currentNodes.clear()
        currentNodes = newNodeSet
    }
}

private fun parseValve(valve: String): Pair<String, Int> {
    val (nameString, flowRateString) = valve.split("has flow rate=")
    return nameString.trim() to flowRateString.trim().toInt()
}

private fun parseTunnels(tunnel: String): List<String> {
    val (_, tunnels) = tunnel.split("to")
    return tunnels.split(" ", ",").mapNotNull {
        if(it.firstOrNull()?.isUpperCase() == true) it.trim() else null
    }
}

fun findMaxPressure(input: List<String>): Int {
    val valves = parseInput(input)
    val valveStrings = valves.map { it.name }.toSet()
    val startingValve = valves.find { it.name == startingNodeName }!!

    val (pressure, path) = calculatePressure(
        state = SearchState(
            currentValve = startingValve,
            totalReleasedPressure = 0,
            remainingTime = 30,
            remainingValves = valveStrings.subtract(setOf(startingValve.name))
        )
    )

    println(path)
    return pressure
}

fun findCombinedMaxPressure(input: List<String>): Int {
    val valves = parseInput(input)
    val valveStrings = valves.map { it.name }.toSet()
    val startingValve = valves.find { it.name == startingNodeName }!!

    val agent = Agent(startingValve, 26)

//    return calculateDuoPressure(
//        state = DuoSearchState(
//            human = agent,
//            elephant = agent,
//            totalReleasedPressure = 0,
//            remainingValves = valveStrings.subtract(setOf(startingValve.name))
//        )
//    )
    return 0
}

fun part2(input: List<String>): Int {
    val valves = parseInput(input)
    val startingValve = valves.find { it.name == startingNodeName }!!

    var result = 0

    val human = Agent(startingValve, 26)
    val elapsedTime = measureTimeMillis {
        val paths = calculateAllPathsWithPressure(human, valves.toSet() - setOf(startingValve), 0)
        println(paths.size)

        val map = mutableMapOf<RemainingValves, Int>()
        paths.forEach { (key, value) ->
            if (!map.contains(key)) map[key] = value
            if(map[key]!! >= value) return@forEach
            map[key] = value
        }


        val maximumReleasedPressure = map.toList().mapIndexed { index, (remainingValves, humanReleasedPressure) ->
            val elephant = Agent(startingValve, 26)
            val elephantReleasedPressure = calculateSinglePressureCached(
                elephant,
                remainingValves,
                0
            )
            println(index)

            //this is the most vile thing I've ever written, yikes xD
            //I should optimize how aggressive I'm caching, caching every inbetween doesn't make sense
            if(index % 500 == 0) mostEfficientSinglePaths.clear()
//            if(
//                remainingValves.find { it.name == "DD" } != null
//                && remainingValves.find { it.name == "HH" } != null
//                && remainingValves.find { it.name == "EE" } != null
//            ) {
//                println("best rotation found $remainingValves, $humanReleasedPressure, $elephantReleasedPressure")
//            }

            elephantReleasedPressure + humanReleasedPressure
        }.max()

        result = maximumReleasedPressure
    }

    println("elapsed time was: $elapsedTime")
    return result
}

data class SearchState(
    val currentValve: Valve,
    val totalReleasedPressure: Int,
    val remainingTime: Int,
    val remainingValves: Set<String>,
    val currentPath: String = startingNodeName,
)

fun calculatePressure(state: SearchState): Pair<Int, String> {
    if(state.remainingValves.isEmpty()) return state.totalReleasedPressure to state.currentPath
    val pressureAndPath = state.remainingValves.map { target ->
        val (destination, travelTime) = state.currentValve.travelTimes?.get(target)!!
        val newRemainingTime = state.remainingTime - travelTime
        if(newRemainingTime < 0) return@map state.totalReleasedPressure to state.currentPath

        val newPath = state.currentPath + " to $target at $newRemainingTime"
        val newReleasedPressure = (newRemainingTime * destination.flowRate) + state.totalReleasedPressure
        if(newRemainingTime < 2) return@map newReleasedPressure to newPath

        val (pressure, path) = calculatePressure(SearchState(
            destination,
            newReleasedPressure,
            newRemainingTime,
            state.remainingValves.subtract(setOf(target)),
            newPath
        ))

        pressure to path
    }.maxBy { it.first }

    return pressureAndPath
}

data class DuoSearchState(
    val human: Agent,
    val elephant: Agent,
    val totalReleasedPressure: Int,
    val remainingValves: Set<String>
)

//fun calculateDuoPressure(state: DuoSearchState): Int {
//    if(state.remainingValves.isEmpty()) return state.totalReleasedPressure
//    //there's at least on valve remaining
//
//    val canHumanContinue = state.human.canContinue()
//    val canElephantContinue = state.elephant.canContinue()
//    val numberOfActiveAgents = if(canHumanContinue && canElephantContinue) 2 else if (canElephantContinue || canHumanContinue) 1 else 0
//
//    if(numberOfActiveAgents == 0) return state.totalReleasedPressure
//    if(numberOfActiveAgents == 1) {
//        val lastAgent = if(canHumanContinue) state.human else state.elephant
//        return state.remainingValves.maxOf { valve ->
//            val (newAgent, releasedPressure)  = lastAgent.travelToAndOpenValve(valve)
//            val newRemainingValves =
//                if(newAgent.hasArrived()) state.remainingValves - setOf(valve) else return state.totalReleasedPressure
//
//            calculatePressure(SearchState(
//                newAgent.currentValve,
//                state.totalReleasedPressure + releasedPressure,
//                newAgent.remainingTime,
//                newRemainingValves
//            )).first
////            releasedPressure + calculateDuoPressure(state.copy(
////                human = if(canHumanContinue) lastAgent else state.human,
////                elephant = if(canElephantContinue) lastAgent else state.elephant,
////                totalReleasedPressure = state.totalReleasedPressure + releasedPressure,
////                remainingValves = newRemainingValves
////            ))
//        }
//    }
//    //Both Agents can continue
//
//    if(state.remainingValves.size == 1) {
//        val lastValve = state.remainingValves.first()
//        return maxOf(
//                state.human.travelToAndOpenValve(lastValve).second,
//                state.elephant.travelToAndOpenValve(lastValve).second
//        ) + state.totalReleasedPressure
//    }
//    //Both agents can continue and there are 2 or more valves left.
//
//    val highestCombinedReleasedPressure = state.remainingValves.flatMap { firstValve ->
//        val (newHumanAgent, humanReleasedPressure) = state.human.travelToAndOpenValve(firstValve)
//        val newRemainingValves =
//            if(newHumanAgent.hasArrived())  state.remainingValves - setOf(firstValve) else state.remainingValves
//
//        val elephantPressure = newRemainingValves.map { secondValve ->
//            val (newElephantAgent, elephantReleasedPressure) = state.elephant.travelToAndOpenValve(secondValve)
//            val newerRemainingValves =
//                if(newElephantAgent.hasArrived())  newRemainingValves - setOf(secondValve) else newRemainingValves
//
//            val pressure= calculateDuoPressure(DuoSearchState(
//                newHumanAgent,
//                newElephantAgent,
//                state.totalReleasedPressure + elephantReleasedPressure + humanReleasedPressure,
//                newerRemainingValves
//            ))
//            pressure
//        }
//
//        elephantPressure + humanReleasedPressure
//    }.max()
//    return highestCombinedReleasedPressure
//}

data class Valve(
    val name: String,
    val flowRate: Int,
    var travelTimes: Map<String, Pair<Valve, Int>>?
) {
    override fun toString() = this.name

    override fun hashCode(): Int {
        return Objects.hash(this.name)
    }
}

data class ValveNode(
    val name: String,
    val flowRate: Int,
    val connections: Set<String>,
)

//idea: do human then elephant?