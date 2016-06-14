package research2016.beliefrevisor.core

import research2016.propositionallogic.Or
import research2016.propositionallogic.Proposition
import research2016.propositionallogic.and
import research2016.propositionallogic.isSatisfiable

/**
 * Created by surpl on 6/13/2016.
 */
interface SentenceRevisionStrategy
{
    fun revise(sentence:Proposition):Proposition
}

class TrustPartitionSentenceRevisionStrategy(val partitions:Set<Proposition>):SentenceRevisionStrategy
{
    override fun revise(sentence:Proposition):Proposition
    {
        return partitions
            // get the intersecting partitions
            .filter {(sentence and it).isSatisfiable}
            // take the union of them by or-ing them together and return that
            .let {Or.make(it)}
    }
}
