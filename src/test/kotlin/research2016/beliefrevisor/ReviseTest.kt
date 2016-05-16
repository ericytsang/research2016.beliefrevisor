package research2016.beliefrevisor

import org.junit.Test
import research2016.propositionallogic.And
import research2016.propositionallogic.BasicProposition
import research2016.propositionallogic.Models
import research2016.propositionallogic.Not
import research2016.propositionallogic.Or
import research2016.propositionallogic.Proposition
import research2016.propositionallogic.Tautology
import research2016.propositionallogic.Xor
import research2016.propositionallogic.models

/**
 * Created by surpl on 5/15/2016.
 */
class ReviseTest()
{
    fun reviseTest(beliefState:Proposition,sentence:Proposition,expected:Models)
    {
        val actual = revise(beliefState,sentence).models
        println("actual: $actual")
        println("expected: $expected")
        assert(actual == expected)
    }

    /**
     * test case where sentence models are a subset of belief state models.
     */
    @Test
    fun reviseSubSetTest()
    {
        val beliefState = Tautology
        val sentence = And(And(BasicProposition.make("p"),BasicProposition.make("q")),BasicProposition.make("r"))
        val expected = And(beliefState,sentence).models
        reviseTest(beliefState,sentence,expected)
    }

    /**
     * test case where sentence models intersect with belief state models.
     */
    @Test
    fun reviseIntersectTest()
    {
        val beliefState = Or(BasicProposition.make("p"),BasicProposition.make("q"))
        val sentence = Or(BasicProposition.make("q"),BasicProposition.make("r"))
        val expected = And(beliefState,sentence).models
        reviseTest(beliefState,sentence,expected)
    }

    /**
     * test case where sentence models don't intersect with belief state models.
     */
    @Test
    fun reviseContradictionTest()
    {
        val beliefState = And(BasicProposition.make("p"),BasicProposition.make("q"))
        val sentence = And(Tautology,Not(beliefState))
        val expected = Xor(BasicProposition.make("p"),BasicProposition.make("q")).models
        reviseTest(beliefState,sentence,expected)
    }
}
