package research2016.beliefrevisor

import org.junit.Test
import research2016.beliefrevisor.core.ComparatorBeliefRevisionStrategy
import research2016.propositionallogic.*
import java.util.*

/**
 * Created by Eric on 5/25/2016.
 */
open class RevisionTest
{
    protected val p = BasicProposition.make("p")
    protected val q = BasicProposition.make("q")
    protected val r = BasicProposition.make("r")

    protected fun reviseTest(beliefState:Set<Proposition>,sentence:Proposition,situationSorterFactory:(Set<Proposition>)-> Comparator<Situation>,expected:Set<Situation>)
    {
        val actual = ComparatorBeliefRevisionStrategy(situationSorterFactory)
            .revise(beliefState,sentence)
            .let {And.make(it.toList())}
            .models
        println("actual: $actual")
        println("expected: $expected")
        assert(actual == expected)
    }
}
