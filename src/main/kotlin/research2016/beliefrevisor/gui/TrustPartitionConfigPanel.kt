package research2016.beliefrevisor.gui

import com.sun.javafx.collections.ObservableListWrapper
import javafx.scene.Node
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextInputDialog
import javafx.scene.layout.VBox
import research2016.beliefrevisor.core.SentenceRevisionStrategy
import research2016.beliefrevisor.core.TrustPartitionSentenceRevisionStrategy
import research2016.propositionallogic.Or
import research2016.propositionallogic.Proposition
import research2016.propositionallogic.Variable
import research2016.propositionallogic.State
import research2016.propositionallogic.generateFrom
import research2016.propositionallogic.makeFrom
import research2016.propositionallogic.not
import research2016.propositionallogic.toParsableString
import java.util.Optional

class TrustPartitionConfigPanel:VBox()
{
    companion object
    {
        const val LABEL_TEXT = "Trust Partition Strategy"
    }

    private val noTrustPartitionStrategy = NoTrust()
    private val completeTrustPartitionStrategy = CompleteTrust()
    private val variablesPartitionStrategy = Variables()
    private val propositionPartitionStrategy = Propositions()

    /**
     * [List] of [Option]s used in the
     * [revisionOperatorComboBox] control.
     */
    private val options:List<Option> = run()
    {
        return@run listOf(completeTrustPartitionStrategy,
            noTrustPartitionStrategy,
            variablesPartitionStrategy,
            propositionPartitionStrategy)
    }

    /**
     * returns a [Comparator] that can be used for belief revision.
     */
    fun sentenceRevisionStrategy(beliefState:Proposition):SentenceRevisionStrategy
    {
        return revisionOperatorComboBox.value.sentenceRevisionStrategy(beliefState)
    }

    fun saveToMap():Map<String,Any>
    {
        // todo
        TODO()
    }

    @Suppress("UNCHECKED_CAST")
    fun loadFromMap(saveMap:Map<String,Any>)
    {
        // todo
        TODO()
    }

    /**
     * [revisionOperatorComboBox] is used by the user to select which revision
     * operator they would like to use.
     */
    private val revisionOperatorComboBox = ComboBox<Option>()
        .apply()
        {
            items = ObservableListWrapper(options)
            value = items.first()
            valueProperty().addListener()
            {
                observableValue,oldValue,newValue ->
                oldValue.settingsPanel?.let()
                {
                    this@TrustPartitionConfigPanel.children.remove(oldValue.settingsPanel)
                }
                newValue.settingsPanel?.let()
                {
                    this@TrustPartitionConfigPanel.children.add(it)
                }
            }
        }

    init
    {
        spacing = Dimens.KEYLINE_SMALL.toDouble()
        children.addAll(Label(LABEL_TEXT),revisionOperatorComboBox)
    }

    private abstract class Option(val name:String)
    {
        abstract val settingsPanel:Node?
        abstract fun sentenceRevisionStrategy(beliefState:Proposition):SentenceRevisionStrategy
        override fun toString():String = name
    }

    private class NoTrust:Option("No Trust")
    {
        override val settingsPanel:Node? = null
        override fun sentenceRevisionStrategy(beliefState:Proposition) = object:SentenceRevisionStrategy
        {
            override fun revise(sentence:Proposition):Proposition
            {
                return beliefState
            }
        }
    }

    private class CompleteTrust:Option("Complete Trust")
    {
        override val settingsPanel:Node? = null
        override fun sentenceRevisionStrategy(beliefState:Proposition) = object:SentenceRevisionStrategy
        {
            override fun revise(sentence:Proposition):Proposition
            {
                return sentence
            }
        }
    }

    private class Variables:Option("Variables")
    {
        override val settingsPanel = object:EditableListView<Variable,TextInputDialog,String>()
        {
            override fun tryParseInput(inputDialog:TextInputDialog):Variable
            {
                return Variable.make(inputDialog.result)
            }

            override fun makeInputDialog(model:Variable?):TextInputDialog
            {
                return TextInputDialog(model?.toString())
                    .apply()
                    {
                        headerText = "Enter the variable name below."
                    }
            }

            override fun isInputCancelled(result:Optional<String>):Boolean
            {
                return !result.isPresent
            }

            override fun tryAddToListAt(existingEntries:MutableList<Variable>,indexOfEntry:Int,newEntry:Variable)
            {
                if (newEntry in existingEntries)
                {
                    throw RuntimeException("\"$newEntry\" already exists.")
                }
                else
                {
                    return existingEntries.add(indexOfEntry,newEntry)
                }
            }
            override fun tryRemoveFromListAt(existingEntries:MutableList<Variable>,indexOfEntry:Int)
            {
                existingEntries.removeAt(indexOfEntry)
            }

            override fun tryUpdateListAt(existingEntries:MutableList<Variable>,indexOfEntry:Int,newEntry:Variable)
            {
                val resultingList = existingEntries.filterIndexed {i,e -> i != indexOfEntry}
                if (newEntry in resultingList)
                {
                    throw RuntimeException("\"$newEntry\" already exists.")
                }
                else
                {
                    existingEntries[indexOfEntry] = newEntry
                }
            }
        }

        override fun sentenceRevisionStrategy(beliefState:Proposition):SentenceRevisionStrategy
        {
            val variables = settingsPanel.listView.items.toSet()
            val states = State.generateFrom(variables)
            val partitions = states.map {Proposition.makeFrom(it)}.toSet()
            return TrustPartitionSentenceRevisionStrategy(partitions)
        }
    }

    private class Propositions:Option("Sentences")
    {
        override val settingsPanel = object:EditableListView<Proposition,TextInputDialog,String>()
        {
            override fun isInputCancelled(result:Optional<String>):Boolean
            {
                return !result.isPresent
            }

            override fun tryParseInput(inputDialog:TextInputDialog):Proposition
            {
                return Proposition.makeFrom(inputDialog.result)
            }

            override fun makeInputDialog(model:Proposition?):TextInputDialog
            {
                return TextInputDialog(model?.toParsableString())
                    .apply {headerText = "Enter the sentence below."}
            }
        }

        override fun sentenceRevisionStrategy(beliefState:Proposition):SentenceRevisionStrategy
        {
            val partitions = settingsPanel.listView.items.toSet()
            return TrustPartitionSentenceRevisionStrategy(partitions+Or.make(partitions.toList()).not)
        }
    }
}
