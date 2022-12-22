fun main() {
    val input = readResourceLines("Day16.txt")
    val output = findMaxPressure(input)
    println("$output")
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
    valves.forEach { valve -> valve.connections = newDestinations[valve]!! }

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
        val (destination, travelTime) = state.currentValve.connections?.get(target)!!
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

data class Valve(
    val name: String,
    val flowRate: Int,
    var connections: Map<String, Pair<Valve, Int>>?
)

data class ValveNode(
    val name: String,
    val flowRate: Int,
    val connections: Set<String>,
)
