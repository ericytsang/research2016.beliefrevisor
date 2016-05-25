package research2016.beliefrevisor

import org.junit.Test
import research2016.propositionallogic.*
import java.util.*

/**
 * Created by surpl on 5/15/2016.
 */
class HammingDistanceRevisionTest():RevisionTest()
{
    /**
     * test case where sentence models are a subset of belief state models.
     */
    @Test
    fun reviseSubSetTest()
    {
        val beliefState = setOf(Tautology)
        val sentence = p and q and r
        val expected = ((beliefState.joinWithOrs() ?: Contradiction) and sentence).models
        reviseTest(beliefState,sentence,{HammingDistanceComparator(it)},expected)
    }

    /**
     * test case where sentence models intersect with belief state models.
     */
    @Test
    fun reviseIntersectTest()
    {
        val beliefState = setOf(p or q)
        val sentence = q or r
        val expected = ((beliefState.joinWithOrs() ?: Contradiction)and sentence).models
        reviseTest(beliefState,sentence,{HammingDistanceComparator(it)},expected)
    }

    /**
     * test case where sentence models don't intersect with belief state models.
     */
    @Test
    fun reviseContradictionTest()
    {
        val beliefState = setOf(p and q)
        val sentence = (Tautology and Not(beliefState.joinWithOrs() ?: Contradiction))
        val expected = Xor(p,q).models
        reviseTest(beliefState,sentence,{HammingDistanceComparator(it)},expected)
    }
}
