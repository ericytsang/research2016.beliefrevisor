package research2016.beliefrevisor

import lib.formulainterpreter.FormulaTreeFactory
import research2016.propositionallogic.And
import research2016.propositionallogic.BasicProposition
import research2016.propositionallogic.Contradiction
import research2016.propositionallogic.Iff
import research2016.propositionallogic.Nand
import research2016.propositionallogic.Not
import research2016.propositionallogic.Oif
import research2016.propositionallogic.Or
import research2016.propositionallogic.Proposition
import research2016.propositionallogic.Tautology
import research2016.propositionallogic.Xor
import research2016.propositionallogic.hammingDistance
import research2016.propositionallogic.isSatisfiable
import research2016.propositionallogic.makeFrom
import research2016.propositionallogic.models
import java.util.regex.Pattern

/**
 * Created by surpl on 5/15/2016.
 */
fun main(args:Array<String>)
{
    try
    {
        val beliefState:Proposition = propositionFactory.parse(prepareForPropositionFactory(args[0]))
        val sentence:Proposition = propositionFactory.parse(prepareForPropositionFactory(args[1]))

        println(revise(beliefState,sentence))
    }
    catch (ex:IndexOutOfBoundsException)
    {
        println("USAGE: java -jar [jarfile.jar] [belief_state] [sentence]")
    }
}

fun revise(beliefState:Proposition,sentence:Proposition):Proposition
{
    // if the belief state and sentence at least intersect...
    return if (And(beliefState,sentence).isSatisfiable)
    {
        // if the sentence is a subset of the belief state
        if (!And(Not(beliefState),sentence).isSatisfiable)
        {
            sentence
        }
        // else the belief state and sentence merely intersect
        else
        {
            And(beliefState,sentence)
        }
    }
    // else the belief state and sentence do not intersect...
    else
    {
        val beliefStateModels = beliefState.models.trueSituations
        val sentenceModels = sentence.models.trueSituations

        // sentence models are grouped by hamming distance.
        // i.e.: Map<hamming distance, model>
        //
        // if there are no beliefStateModels, then all models are mapped to 0
        val orderedSentenceModels = sentenceModels.groupBy()
        {
            situation ->
            val nearestSituation = beliefStateModels.minBy {situation.hammingDistance(it)}
            nearestSituation?.hammingDistance(situation) ?: 0
        }

        // return the group of models with the lowest hamming distance as a
        // proposition
        val smallestDistance = orderedSentenceModels.keys.min()
        val revisedBeliefState = orderedSentenceModels[smallestDistance] ?: emptyList()
        Proposition.makeFrom(revisedBeliefState)
    }
}

fun prepareForPropositionFactory(string:String):List<String> = string.replace("("," ( ").replace(")"," ) ").replace("-"," - ").trim().split(Regex("[ ]+"))

val propositionFactory = FormulaTreeFactory(

    object:FormulaTreeFactory.TokenInterpreter
    {
        override fun parse(word:String):FormulaTreeFactory.Symbol
        {
            val preprocessedWord = word.toLowerCase().trim()
            return when
            {
                Pattern.matches("(iff){1}",preprocessedWord) -> FormulaTreeFactory.Symbol(FormulaTreeFactory.Symbol.Type.OPERATOR,2,1)
                Pattern.matches("(then){1}",preprocessedWord) -> FormulaTreeFactory.Symbol(FormulaTreeFactory.Symbol.Type.OPERATOR,2,2)
                Pattern.matches("(or){1}",preprocessedWord) ||
                    Pattern.matches("(xor){1}",preprocessedWord) -> FormulaTreeFactory.Symbol(FormulaTreeFactory.Symbol.Type.OPERATOR,2,3)
                Pattern.matches("(and){1}",preprocessedWord) ||
                    Pattern.matches("(nand){1}",preprocessedWord) -> FormulaTreeFactory.Symbol(FormulaTreeFactory.Symbol.Type.OPERATOR,2,4)
                Pattern.matches("(-){1}",preprocessedWord) -> FormulaTreeFactory.Symbol(FormulaTreeFactory.Symbol.Type.OPERATOR,1,5)
                Pattern.matches("(1){1}",preprocessedWord) ||
                    Pattern.matches("(0){1}",preprocessedWord) ||
                    Pattern.matches("[a-zA-Z]+",preprocessedWord) -> FormulaTreeFactory.Symbol(FormulaTreeFactory.Symbol.Type.OPERAND,0,0)
                Pattern.matches("[(]{1}",preprocessedWord) -> FormulaTreeFactory.Symbol(FormulaTreeFactory.Symbol.Type.OPENING_PARENTHESIS,0,0)
                Pattern.matches("[)]{1}",preprocessedWord) -> FormulaTreeFactory.Symbol(FormulaTreeFactory.Symbol.Type.CLOSING_PARENTHESIS,0,0)
                else -> throw IllegalArgumentException("unrecognized token: $word")
            }
        }
    },

    object:FormulaTreeFactory.OperandFactory<Proposition>
    {
        override fun parse(word:String):Proposition
        {
            val preprocessedWord = word.toLowerCase().trim()
            return when
            {
                Pattern.matches("(1){1}",preprocessedWord) -> Tautology
                Pattern.matches("(0){1}",preprocessedWord) -> Contradiction
                Pattern.matches("[a-zA-Z]+",preprocessedWord) -> BasicProposition.make(preprocessedWord)
                else -> throw IllegalArgumentException("unrecognized token: $word")
            }
        }

        override fun parse(word:String,operands:List<Proposition>):Proposition
        {
            val preprocessedWord = word.toLowerCase().trim()
            return when
            {
                Pattern.matches("(iff){1}",preprocessedWord) -> Iff(operands.first(),operands.last())
                Pattern.matches("(then){1}",preprocessedWord) -> Oif(operands.first(),operands.last())
                Pattern.matches("(or){1}",preprocessedWord) -> Or(operands.first(),operands.last())
                Pattern.matches("(xor){1}",preprocessedWord) -> Xor(operands.first(),operands.last())
                Pattern.matches("(and){1}",preprocessedWord) -> And(operands.first(),operands.last())
                Pattern.matches("(nand){1}",preprocessedWord) -> Nand(operands.first(),operands.last())
                Pattern.matches("(-){1}",preprocessedWord) -> Not(operands.single())
                else -> throw IllegalArgumentException("unrecognized token: $word")
            }
        }
    })
