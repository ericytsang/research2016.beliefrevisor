package research2016.beliefrevisor

import research2016.propositionallogic.And
import research2016.propositionallogic.Contradiction
import research2016.propositionallogic.Proposition
import research2016.propositionallogic.Situation
import research2016.propositionallogic.models
import java.util.Comparator
import java.util.LinkedHashMap

/**
 * Created by surpl on 5/20/2016.
 */

/**
 * used to compare [Situation]s with one another, in order to sort them.
 */
interface SituationSorter:Comparator<Situation>
{
    /**
     * compares [situation1] with [situation2]. if [situation1] is supposed to
     * precede [situation2] in this ordering, a negative number should be
     * returned. if [situation1] is equal to [situation2], zero should be
     * returned. if [situation1] is larger than [situation2], a positive number
     * should be returned.
     */
    override fun compare(situation1:Situation,situation2:Situation):Int
}

class HammingDistanceSituationSorter(beliefState:Set<Proposition>):SituationSorter
{
    /**
     * all models of the initial belief state; the belief state that is being
     * revised.
     */
    private val beliefStateModels:Set<Situation> = run()
    {
        val concatenatedBeliefState = beliefState.fold<Proposition,Proposition?>(null)
        {
            initial,next ->
            initial?.let {And(initial,next)} ?: next
        } ?: Contradiction
        concatenatedBeliefState.models.trueSituations
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
            beliefStateModels.map {hammingDistance(situation1,it)}.min() ?: 0
        }
        val situation2Distance = cachedCalculations.getOrPut(situation2)
        {
            beliefStateModels.map {hammingDistance(situation2,it)}.min() ?: 0
        }
        return situation1Distance-situation2Distance
    }

    /**
     * returns the hamming distance between this [situation1] and [situation2];
     * the number of mappings of variables to truth values that they do not
     * match.
     */
    private fun hammingDistance(situation1:Situation,situation2:Situation):Int
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
