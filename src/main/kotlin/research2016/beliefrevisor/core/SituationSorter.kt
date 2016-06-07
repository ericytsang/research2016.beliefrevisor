package research2016.beliefrevisor.core

import research2016.propositionallogic.*
import java.util.Comparator
import java.util.LinkedHashMap

/**
 * [beliefState] is the belief state that is being revised.
 *
 * the [ByDistanceComparator] is a [Comparator] that can compare [Situation]
 * with one another to specify an ordering.
 *
 * this implementation assumes that each [Situation] is a specific distance away
 * from the [beliefState], and implements the [compare] function with this
 * assumption.
 */
abstract class ByDistanceComparator():Comparator<Situation>
{
    /**
     * all models of the receiver.
     */
    protected fun Iterable<Proposition>.models():Set<Situation>
    {
        return let()
        {
            if (!it.iterator().hasNext())
            {
                Contradiction
            }
            else
            {
                And.make(it.toList())
            }
        }.models
    }

    /**
     * used to cache previous calculations produced by the [computeDistance]
     * function.
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

    /**
     * returns the distance from the [situation] to the [beliefState].
     */
    protected abstract fun computeDistance(situation:Situation):Int
}

class HammingDistanceComparator(beliefState:Set<Proposition>):ByDistanceComparator()
{
    private val beliefStateModels = beliefState.models()

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

class WeightedHammingDistanceComparator(beliefState:Set<Proposition>,val weights:Map<Variable,Int>):ByDistanceComparator()
{
    private val beliefStateModels = beliefState.models()

    override fun computeDistance(situation:Situation):Int
    {
        return beliefStateModels.map {weightedHammingDistance(situation,it)}.min() ?: 0
    }

    /**
     * returns the weighted hamming distance between this [situation1] and
     * [situation2]; the number of mappings of variables to truth values that
     * they do not match multiplied by their respective weights, then summed
     * together.
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

class OrderedSetsComparator(val beliefState:Set<Proposition>,val orderedSets:List<Proposition>):ByDistanceComparator()
{
    override fun computeDistance(situation:Situation):Int
    {
        val completeOrderedSets = listOf(And.make(beliefState.toList()))+orderedSets+Tautology
        return completeOrderedSets.indexOfFirst {it.truthiness(situation) == 1.0}
    }
}
