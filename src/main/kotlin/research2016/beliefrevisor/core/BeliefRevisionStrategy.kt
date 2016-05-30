package research2016.beliefrevisor.core

import research2016.propositionallogic.And
import research2016.propositionallogic.Proposition
import research2016.propositionallogic.Situation
import research2016.propositionallogic.and
import research2016.propositionallogic.basicPropositions
import research2016.propositionallogic.evaluate
import research2016.propositionallogic.generateFrom
import research2016.propositionallogic.makeFrom
import research2016.propositionallogic.models
import research2016.propositionallogic.not
import research2016.propositionallogic.or
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

        val basicPropositionTautologies = (setOf(sentence)+beliefState)
            // get all basic propositions involved
            .flatMap {it.basicPropositions}.toSet()
            // make each one into a tautology
            .map {it or it.not}
            // and them together
            .let {And.make(it)}

        // all models of the sentence..and'd together with
        // basicPropositionTautologies to make sure the models involves all
        // variables
        val sentenceModels = (sentence and basicPropositionTautologies).models

        val nearestModel = sentenceModels.minWith(situationSorter)
        val nearestSituations = sentenceModels

            // keep only the ones with the least distance according to the sorter
            .filter {situationSorter.compare(nearestModel,it) == 0}

        // convert into a proposition and return
        return nearestSituations.map {Proposition.makeFrom(it)}.toSet()
    }
}
