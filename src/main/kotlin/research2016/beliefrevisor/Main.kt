package research2016.beliefrevisor

import research2016.propositionallogic.Proposition
import research2016.propositionallogic.makeFrom

fun main(args:Array<String>)
{
    try
    {
        val beliefState:Proposition = Proposition.makeFrom(args[0])
        val sentence:Proposition = Proposition.makeFrom(args[1])

        val beliefRevisor = TotalPreOrderBeliefRevisionStrategy({HammingDistanceComparator(it)})

        println(beliefRevisor.revise(setOf(beliefState),sentence))
    }
    catch (ex:IndexOutOfBoundsException)
    {
        println("USAGE: java -jar [jarfile.jar] [belief_state] [sentence]")
    }
}
