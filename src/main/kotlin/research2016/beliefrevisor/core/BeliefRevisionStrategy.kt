package research2016.beliefrevisor.core

import research2016.propositionallogic.Proposition
import research2016.propositionallogic.Situation
import research2016.propositionallogic.basicPropositions
import research2016.propositionallogic.evaluate
import research2016.propositionallogic.generateFrom
import research2016.propositionallogic.makeFrom
import research2016.propositionallogic.models
import java.util.Comparator

interface BeliefRevisionStrategy
{
    fun revise(beliefState:Set<Proposition>,sentence:Proposition):Set<Proposition>
}

/**
 * class that uses a [Comparator] to order [Situation]s in order to do create
 * the "total pre-order" for the belief revision.
 *
 * the [situationSorterFactory] is used to create the [Comparator]. it will be
 * given the initial belief state as an argument, and must return the
 * appropriate [Comparator] which will be used to sort the [Situation]s.
 */
class TotalPreOrderBeliefRevisionStrategy(val situationSorterFactory:(Set<Proposition>)->Comparator<Situation>):BeliefRevisionStrategy
{
    override fun revise(beliefState:Set<Proposition>,sentence:Proposition):Set<Proposition>
    {
        // create the situation sorter
        val situationSorter = situationSorterFactory(beliefState)
        val orderedSentenceModels = Situation

            // find all models of the sentence
            .generateFrom((setOf(sentence)+beliefState).flatMap {it.basicPropositions}.toSet())
            .filter {sentence.evaluate(it)}

            // sort them using the situation sorter
            .sortedWith(situationSorter)

        val nearestSituations = orderedSentenceModels

            // keep only the ones with the least distance according to the sorter
            .filter {situationSorter.compare(orderedSentenceModels.first(),it) == 0}

        // convert into a proposition and return
        return nearestSituations.map {Proposition.makeFrom(it)}.toSet()
    }
}
