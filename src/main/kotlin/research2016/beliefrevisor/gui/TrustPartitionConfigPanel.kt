package research2016.beliefrevisor.gui

import com.sun.javafx.collections.ObservableListWrapper
import javafx.beans.InvalidationListener
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
import research2016.propositionallogic.Tautology
import research2016.propositionallogic.generateFrom
import research2016.propositionallogic.makeFrom
import research2016.propositionallogic.not
import research2016.propositionallogic.toParsableString
import java.io.Serializable
import java.util.Optional

class TrustPartitionConfigPanel:VBox()
{
    companion object
    {
        const val LABEL_TEXT = "Trust Partition Strategy"
        const val SAVE_MAP_VARIABLES = "SAVE_MAP_VARIABLES"
        const val SAVE_MAP_SENTENCES = "SAVE_MAP_SENTENCES"
    }

    private val noTrustPartitionStrategy = NoTrust()
    private val completeTrustPartitionStrategy = CompleteTrust()
    private val variablesPartitionStrategy = Variables()
    private val propositionPartitionStrategy = Propositions()

    var listener:InvalidationListener? = null

    /**
     * [List] of [Option]s used in the
     * [trustPartitionComboBox] control.
     */
    private val options:List<Option> = run()
    {
        return@run listOf(completeTrustPartitionStrategy,
            noTrustPartitionStrategy,
            variablesPartitionStrategy,
            propositionPartitionStrategy)
    }

    val sentenceRevisionStrategy:SentenceRevisionStrategy? get()
    {
        return trustPartitionComboBox.value.sentenceRevisionStrategy
    }

    fun saveToMap():Map<String,Any>
    {
        val saveMap = mutableMapOf<String,Any>()
        saveMap.put(SAVE_MAP_VARIABLES,variablesPartitionStrategy.settingsPanel.listView.items.toList() as Serializable)
        saveMap.put(SAVE_MAP_SENTENCES,propositionPartitionStrategy.settingsPanel.listView.items.toList() as Serializable)
        return saveMap
    }

    @Suppress("UNCHECKED_CAST")
    fun loadFromMap(saveMap:Map<String,Any>)
    {
        variablesPartitionStrategy.settingsPanel.listView.items.clear()
        variablesPartitionStrategy.settingsPanel.listView.items.addAll(saveMap[SAVE_MAP_VARIABLES] as List<Variable>? ?: emptyList())
        propositionPartitionStrategy.settingsPanel.listView.items.clear()
        propositionPartitionStrategy.settingsPanel.listView.items.addAll(saveMap[SAVE_MAP_SENTENCES] as List<Proposition>? ?: emptyList())
    }

    /**
     * [trustPartitionComboBox] is used by the user to select which revision
     * operator they would like to use.
     */
    private val trustPartitionComboBox = ComboBox<Option>().apply()
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
            listener?.invalidated(null)
        }
    }

    init
    {
        spacing = Dimens.KEYLINE_SMALL.toDouble()
        children.addAll(Label(LABEL_TEXT),trustPartitionComboBox)
    }

    private abstract class Option(val name:String)
    {
        abstract val settingsPanel:Node?
        abstract val sentenceRevisionStrategy:SentenceRevisionStrategy?
        override fun toString():String = name
    }

    private inner class NoTrust:Option("No Trust")
    {
        override val settingsPanel:Node? = null
        override val sentenceRevisionStrategy = object:SentenceRevisionStrategy
        {
            override fun revise(sentence:Proposition):Proposition
            {
                return Tautology
            }
        }
    }

    private inner class CompleteTrust:Option("Complete Trust")
    {
        override val settingsPanel:Node? = null
        override val sentenceRevisionStrategy = object:SentenceRevisionStrategy
        {
            override fun revise(sentence:Proposition):Proposition
            {
                return sentence
            }
        }
    }

    private inner class Variables:Option("Variables")
    {
        override val settingsPanel = object:EditableListView<Variable,TextInputDialog,String>()
        {
            init
            {
                prefHeight = 100.0
                minHeight = prefHeight
                listView.items.addListener(InvalidationListener {listener?.invalidated(null)})
            }

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

        override val sentenceRevisionStrategy:SentenceRevisionStrategy? get()
        {
            val variables = settingsPanel.listView.items.toSet()
            val states = State.generateFrom(variables)
            if (states.isNotEmpty())
            {
                val partitions = states.map {Proposition.makeFrom(it)}.toSet()
                return TrustPartitionSentenceRevisionStrategy(partitions)
            }
            else
            {
                return null
            }
        }
    }

    private inner class Propositions:Option("Sentences")
    {
        override val settingsPanel = object:EditableListView<Proposition,TextInputDialog,String>()
        {
            init
            {
                prefHeight = 100.0
                minHeight = prefHeight
                listView.items.addListener(InvalidationListener {listener?.invalidated(null)})
            }

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

        override val sentenceRevisionStrategy:SentenceRevisionStrategy? get()
        {
            val partitions = settingsPanel.listView.items.toSet()
            if (partitions.isNotEmpty())
            {
                return TrustPartitionSentenceRevisionStrategy(partitions)
            }
            else
            {
                return null
            }
        }
    }
}
