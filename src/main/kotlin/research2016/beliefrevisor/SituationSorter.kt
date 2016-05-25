package research2016.beliefrevisor

import research2016.propositionallogic.*
import java.util.Comparator
import java.util.LinkedHashMap

abstract class ByDistanceComparator(val beliefState:Set<Proposition>):Comparator<Situation>
{
    /**
     * all models of the initial belief state; the belief state that is being
     * revised.
     */
    protected val beliefStateModels:Set<Situation> = run()
    {
        val concatenatedBeliefState = beliefState.fold<Proposition,Proposition?>(null)
        {
            initial,next ->
            initial?.let {initial and next} ?: next
        } ?: Contradiction
        concatenatedBeliefState.models
    }

    /**
     * used to cache previous calculations of the minimum distance between a
     * [Situation] and the [beliefStateModels].
     */
    private val cachedCalculations = LinkedHashMap<Situation,Int>()

    override fun compare(situation1:Situation,situation2:Situation):Int
    {
        val situation1Distance = cachedCalculations.getOrPut(situation1)
        {
            computeDistance(situation1)
        }
        val situation2Distance = cachedCalculations.getOrPut(situation2)
        {
            computeDistance(situation2)
        }
        return situation1Distance-situation2Distance
    }

    protected abstract fun computeDistance(situation:Situation):Int
}

class HammingDistanceComparator(beliefState:Set<Proposition>):ByDistanceComparator(beliefState)
{
    override fun computeDistance(situation:Situation):Int
    {
        return beliefStateModels.map {hammingDistance(situation,it)}.min() ?: 0
    }

    /**
     * returns the hamming distance between this [situation1] and [situation2];
     * the number of mappings of variables to truth values that they do not
     * match.
     */
    fun hammingDistance(situation1:Situation,situation2:Situation):Int
    {
        val commonKeys = if (situation1.keys.size < situation2.keys.size)
        {
            situation1.keys.intersect(situation2.keys)
        }
        else
        {
            situation2.keys.intersect(situation1.keys)
        }

        return commonKeys.count {situation1[it] != situation2[it]}
    }
}

class WeightedHammingDistanceComparator(beliefState:Set<Proposition>,val weights:Map<BasicProposition,Int>):ByDistanceComparator(beliefState)
{
    override fun computeDistance(situation:Situation):Int
    {
        return beliefStateModels.map {weightedHammingDistance(situation,it)}.min() ?: 0
    }

    /**
     * returns the hamming distance between this [situation1] and [situation2];
     * the number of mappings of variables to truth values that they do not
     * match.
     */
    fun weightedHammingDistance(situation1:Situation,situation2:Situation):Int
    {
        val commonKeys = if (situation1.keys.size < situation2.keys.size)
        {
            situation1.keys.intersect(situation2.keys)
        }
        else
        {
            situation2.keys.intersect(situation1.keys)
        }

        return commonKeys
            // only consider the basic propositions that situations disagree on
            .filter {situation1[it] != situation2[it]}
            // sum them by their weights
            .sumBy {weights[it] ?: 0}
    }
}

class SetInclusionComparator(beliefState:Set<Proposition>,val biases:Set<Proposition> = beliefState):ByDistanceComparator(beliefState)
{
    override fun computeDistance(situation:Situation):Int
    {
        return biases.count {it.truthiness(situation) == 1.0}.let {-it}
    }
}
