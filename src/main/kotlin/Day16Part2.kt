typealias RemainingValves = Set<Valve>
fun calculateAllPaths(agent: Agent, remainingValves: RemainingValves): List<RemainingValves> {
    val list = remainingValves.flatMap { valve ->
        val (traveledAgent, releasedPressure) = agent.travelToAndOpenValve(valve.name)
        if(traveledAgent == null) return@flatMap listOf(remainingValves)
        calculateAllPaths(traveledAgent, remainingValves - setOf(valve))
    }

    return list + listOf(remainingValves)
}

fun calculateAllPathsWithPressure(agent: Agent, remainingValves: RemainingValves, totalReleasedPressure: Int): List<Pair<RemainingValves, Int>> {
    if(remainingValves.isEmpty()) return listOf(emptySet<Valve>() to totalReleasedPressure)

    val list = remainingValves.flatMap { valve ->
        val (traveledAgent, releasedPressure) = agent.travelToAndOpenValve(valve.name)
        if(traveledAgent == null) return@flatMap listOf(remainingValves to totalReleasedPressure)
        calculateAllPathsWithPressure(traveledAgent, remainingValves - setOf(valve), totalReleasedPressure + releasedPressure)
    }

    return list + listOf(remainingValves to totalReleasedPressure)
}



val mostEfficientSinglePaths: MutableMap<Pair<Set<Valve>, Agent>, Int> = mutableMapOf()
fun calculateSinglePressureCached(agent: Agent, remainingValves: Set<Valve>, totalReleasedPressure: Int): Int {
    if(!agent.canContinue()) return totalReleasedPressure
    if(remainingValves.isEmpty()) return totalReleasedPressure

    val key = remainingValves to agent
    val cachedValue = mostEfficientSinglePaths[key]
    if(cachedValue != null) return cachedValue

    val maxPressure = remainingValves.maxOf { valve ->
        val (traveledAgent, releasedPressure) = agent.travelToAndOpenValve(valve.name)
        if(traveledAgent == null) return@maxOf totalReleasedPressure + 0

        val newRemainingValves = remainingValves - setOf(valve)
        calculateSinglePressureCached(
            traveledAgent,
            newRemainingValves,
            totalReleasedPressure + releasedPressure
        )
    }

    mostEfficientSinglePaths[remainingValves to agent] = maxPressure
    return maxPressure
}

class Agent(
    private val currentValve: Valve,
    private val remainingTime: Int,
//    private val valveCounter: Int,
) {
//    fun travel(time: Int): Agent? {
//        val newRemainingTime = remainingTime - time
//        return if(newRemainingTime < 0) null else
//        return this.copy(remainingTime = newRemainingTime)
//    }
    fun hasArrived() = remainingTime >= 0
    fun canContinue() = remainingTime >= 2

    fun travelToAndOpenValve(targetName: String): Pair<Agent?, Int> {
        val (target , travelTime) = this.currentValve.travelTimes?.get(targetName)!!


        val newRemainingTime = remainingTime - travelTime
        if(newRemainingTime < 0) return null to 0

        val arrivedAgent = Agent(
            currentValve = target,
            remainingTime = newRemainingTime,
//            valveCounter = this.valveCounter + 1
        //this breaks equivalence checks
        )
        //println("agent opened valve: $targetName, ${arrivedAgent.remainingTime}, $valveCounter")
        return arrivedAgent to arrivedAgent.remainingTime * target.flowRate
    }
}