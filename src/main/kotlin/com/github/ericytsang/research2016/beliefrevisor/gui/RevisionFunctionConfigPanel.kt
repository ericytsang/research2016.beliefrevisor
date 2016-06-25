package com.github.ericytsang.research2016.beliefrevisor.gui

import com.sun.javafx.collections.ObservableListWrapper
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextInputDialog
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import com.github.ericytsang.lib.collections.getRandom
import com.github.ericytsang.research2016.propositionallogic.BeliefRevisionStrategy
import com.github.ericytsang.research2016.propositionallogic.ComparatorBeliefRevisionStrategy
import com.github.ericytsang.research2016.propositionallogic.HammingDistanceComparator
import com.github.ericytsang.research2016.propositionallogic.Or
import com.github.ericytsang.research2016.propositionallogic.OrderedSetsComparator
import com.github.ericytsang.research2016.propositionallogic.Proposition
import com.github.ericytsang.research2016.propositionallogic.SatisfiabilityBeliefRevisionStrategy
import com.github.ericytsang.research2016.propositionallogic.State
import com.github.ericytsang.research2016.propositionallogic.Variable
import com.github.ericytsang.research2016.propositionallogic.WeightedHammingDistanceComparator
import com.github.ericytsang.research2016.propositionallogic.generateFrom
import com.github.ericytsang.research2016.propositionallogic.makeFrom
import com.github.ericytsang.research2016.propositionallogic.toParsableString
import java.io.Serializable
import java.util.Collections
import java.util.Optional

class RevisionFunctionConfigPanel():VBox()
{
    companion object
    {
        const val LABEL_TEXT = "Belief Revision Strategy"
        const val SAVE_MAP_WEIGHTED_HAMMING_DISTANCE = "SAVE_MAP_WEIGHTED_HAMMING_DISTANCE"
        const val SAVE_MAP_ORDERED_SETS = "SAVE_MAP_ORDERED_SETS"
        const val BUTTON_MAKE_RANDOMORDERED_SET = "Generate random order"
    }

    private val hammingDistanceRevisionOperatorOption = HammingDistanceOption()
    private val weightedHammingDistanceRevisionOperatorOption = WeightedHammingDistanceOption()
    private val setInclusionRevisionOperatorOption = SatisfiabilityOption()
    private val orderedSetsRevisionOperatorOption = OrderedSetsOption()

    var listener:InvalidationListener? = null

    /**
     * [List] of [Option]s used in the
     * [revisionOperatorComboBox] control.
     */
    private val options:List<Option> = run()
    {
        return@run listOf(hammingDistanceRevisionOperatorOption,
            weightedHammingDistanceRevisionOperatorOption,
            setInclusionRevisionOperatorOption,
            orderedSetsRevisionOperatorOption)
    }

    /**
     * returns a [Comparator] that can be used for belief revision.
     */
    val beliefRevisionStrategy:BeliefRevisionStrategy?
        get() = revisionOperatorComboBox.value.beliefRevisionStrategy

    fun saveToMap():Map<String,Any>
    {
        val saveMap = mutableMapOf<String,Any>()
        saveMap.put(SAVE_MAP_WEIGHTED_HAMMING_DISTANCE,weightedHammingDistanceRevisionOperatorOption.settingsPanel.listView.items.toList() as Serializable)
        saveMap.put(SAVE_MAP_ORDERED_SETS,orderedSetsRevisionOperatorOption.settingsPanel.listView.items.map {it.map {it}} as Serializable)
        return saveMap
    }

    @Suppress("UNCHECKED_CAST")
    fun loadFromMap(saveMap:Map<String,Any>)
    {
        weightedHammingDistanceRevisionOperatorOption.settingsPanel.listView.items.clear()
        weightedHammingDistanceRevisionOperatorOption.settingsPanel.listView.items.addAll(saveMap[SAVE_MAP_WEIGHTED_HAMMING_DISTANCE] as List<Mapping>? ?: emptyList())
        orderedSetsRevisionOperatorOption.settingsPanel.listView.items.clear()
        orderedSetsRevisionOperatorOption.settingsPanel.listView.items.addAll(saveMap[SAVE_MAP_ORDERED_SETS] as List<List<Proposition>>? ?: emptyList())
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
                    this@RevisionFunctionConfigPanel.children.remove(oldValue.settingsPanel)
                }
                newValue.settingsPanel?.let()
                {
                    this@RevisionFunctionConfigPanel.children.add(it)
                }
                listener?.invalidated(null)
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
        abstract val beliefRevisionStrategy:BeliefRevisionStrategy?
        override fun toString():String = name
    }

    private inner class HammingDistanceOption:Option("Hamming Distance")
    {
        override val settingsPanel:Node? = null
        override val beliefRevisionStrategy:BeliefRevisionStrategy
            get()
            {
                return ComparatorBeliefRevisionStrategy()
                {
                    initialBeliefState:Set<Proposition> ->
                    HammingDistanceComparator(initialBeliefState)
                }
            }
    }

    private inner class WeightedHammingDistanceOption:Option("Weighted Hamming Distance")
    {
        override val settingsPanel = object:EditableListView<Mapping,TextInputDialog,String>()
        {
            init
            {
                setVgrow(this,Priority.ALWAYS)
            }

            override fun tryParseInput(inputDialog:TextInputDialog):Mapping
            {
                val subStrings = inputDialog.result.split("=")
                if (subStrings.size != 2)
                {
                    throw IllegalArgumentException("an equals sign must be used to represent the mapping e.g. \"a = 5\"")
                }
                if (!(subStrings[0].trim().matches(Regex("[a-zA-Z]+"))))
                {
                    throw IllegalArgumentException("the variable name \"${subStrings[0].trim()}\" may only contain alphabetic characters.")
                }
                if (!(subStrings[1].trim().matches(Regex("[0-9]+"))))
                {
                    throw IllegalArgumentException("the variable weight \"${subStrings[1].trim()}\" may only contain numeric characters.")
                }
                val variableName = subStrings[0].trim()
                val weight = subStrings[1].trim().toInt()
                return Mapping(variableName,weight)
            }

            override fun makeInputDialog(model:Mapping?):TextInputDialog
            {
                return TextInputDialog(model?.toString())
                    .apply {headerText = "Enter the mapping below"}
            }

            override fun isInputCancelled(result:Optional<String>):Boolean
            {
                return !result.isPresent
            }

            override fun tryAddToListAt(existingEntries:MutableList<Mapping>,indexOfEntry:Int,newEntry:Mapping)
            {
                if (existingEntries.any {it.variableName == newEntry.variableName})
                {
                    throw RuntimeException("A mapping for the variable \"${newEntry.variableName}\" already exists.")
                }
                else
                {
                    existingEntries.add(indexOfEntry,newEntry)
                }
            }

            override fun tryRemoveFromListAt(existingEntries:MutableList<Mapping>,indexOfEntry:Int)
            {
                existingEntries.removeAt(indexOfEntry)
            }

            override fun tryUpdateListAt(existingEntries:MutableList<Mapping>,indexOfEntry:Int,newEntry:Mapping)
            {
                val resultingList = existingEntries.filterIndexed {i,e -> i != indexOfEntry}
                if (resultingList.any {it.variableName == newEntry.variableName})
                {
                    throw RuntimeException("A mapping for the variable \"${newEntry.variableName}\" already exists.")
                }
                else
                {
                    existingEntries[indexOfEntry] = newEntry
                }
            }
        }

        override val beliefRevisionStrategy:BeliefRevisionStrategy get()
        {
            return ComparatorBeliefRevisionStrategy()
            {
                initialBeliefState:Set<Proposition> ->
                val weights = settingsPanel.listView.items.associate {Variable.make(it.variableName) to it.weight}
                WeightedHammingDistanceComparator(initialBeliefState,weights)
            }
        }
    }

    class Mapping(val variableName:String,val weight:Int):Serializable
    {
        override fun toString():String = "$variableName = $weight"
    }

    private inner class SatisfiabilityOption:Option("Satisfiability")
    {
        override val settingsPanel = null
        override val beliefRevisionStrategy:BeliefRevisionStrategy = SatisfiabilityBeliefRevisionStrategy()
    }

    private inner class OrderedSetsOption:Option("Ordered Sets")
    {
        override val settingsPanel = object:EditableListView<List<Proposition>,Alert,ButtonType>()
        {
            init
            {
                setVgrow(this,Priority.ALWAYS)
            }

            override fun tryParseInput(inputDialog:Alert):List<Proposition>
            {
                @Suppress("UNCHECKED_CAST")
                return ((inputDialog.dialogPane.content as VBox).children.first() as EditableListView<Proposition,TextInputDialog,String>)
                    .listView.items
            }

            override fun makeInputDialog(model:List<Proposition>?):Alert
            {
                return Alert(Alert.AlertType.NONE).apply()
                {
                    buttonTypes.addAll(ButtonType.CANCEL,ButtonType.OK)
                    headerText = "Enter sentences below."
                    dialogPane.content = makeInputDialogContent(model)
                    onShown = EventHandler()
                    {
                        Platform.runLater()
                        {
                            ((dialogPane.content as Parent).childrenUnmodifiable.first() as Parent).childrenUnmodifiable.first().requestFocus()
                        }
                    }
                }
            }

            override fun isInputCancelled(result:Optional<ButtonType>):Boolean
            {
                return result.get() == ButtonType.CANCEL
            }
        }

        fun makeInputDialogContent(model:List<Proposition>?):Node
        {
            val listview = object:EditableListView<Proposition,TextInputDialog,String>()
            {
                init
                {
                    if (model?.isNotEmpty() == true)
                    {
                        listView.items.addAll(model!!)
                    }
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
                        .apply()
                        {
                            headerText = "Enter the sentence below."
                        }
                }
            }
            val makeRandomButton = Button(BUTTON_MAKE_RANDOMORDERED_SET).apply()
            {
                onAction = EventHandler()
                {
                    val dialogTitles = "Generate Random Ordering"
                    val variablesPrompt = "Enter a comma separated list of all the variables below."
                    val variablesParser = {string:String -> string.split(",").map {Variable.make(it.trim())}.toSet()}
                    val numBucketsPrompt = "Enter the number of buckets to sort generated states into."
                    val numBucketsParser = {string:String -> string.toInt()}

                    // get input from user
                    val variables:Set<Variable> = getInputByTextInputDialog(dialogTitles,variablesPrompt,variablesParser) ?: return@EventHandler
                    val numBuckets:Int = getInputByTextInputDialog(dialogTitles,numBucketsPrompt,numBucketsParser) ?: return@EventHandler

                    // randomized list of all possible states involving all
                    // variables in variables represented by variable
                    // conjunctions.
                    val allStates = State.generateFrom(variables)
                        .map {Proposition.makeFrom(it)}
                        .toMutableList()
                        .apply {Collections.shuffle(this)}
                        .iterator()

                    // create numBucket dnf sentences that are  elements in
                    // allStates concatenated with OR operators.
                    val dnfSentences = Array<MutableSet<Proposition>>(numBuckets,{mutableSetOf()})
                        .apply()
                        {
                            for (i in indices)
                            {
                                if (allStates.hasNext())
                                {
                                    this[i].add(allStates.next())
                                }
                                else
                                {
                                    break
                                }
                            }
                            allStates.forEach {getRandom().add(it)}
                        }
                        .filter {it.isNotEmpty()}.map {Or.make(it.toList())}

                    // add all the dnf sentences to the listView.
                    listview.listView.items.clear()
                    listview.listView.items.addAll(dnfSentences)
                }
            }
            return VBox().apply()
            {
                spacing = Dimens.KEYLINE_SMALL.toDouble()
                children.addAll(listview,makeRandomButton)
            }
        }

        init
        {
            settingsPanel.listView.focusModel.focusedItemProperty().addListener(InvalidationListener()
            {
                listener?.invalidated(null)
            })
        }

        override val beliefRevisionStrategy:BeliefRevisionStrategy? get()
        {
            if (settingsPanel.listView.focusModel.focusedItem != null)
            {
                return ComparatorBeliefRevisionStrategy()
                {
                    initialBeliefState:Set<Proposition> ->
                    OrderedSetsComparator(initialBeliefState,settingsPanel.listView.focusModel.focusedItem)
                }
            }
            else
            {
                return null
            }
        }
    }

    private fun <R> getInputByTextInputDialog(dialogTitle:String,promptText:String,parseInput:(String)->R):R?
    {
        val textInputDialog = TextInputDialog().apply()
        {
            title = dialogTitle
            headerText = promptText
        }

        while (true)
        {
            // get the variables input
            val result = textInputDialog.showAndWait()

            // abort if the operation is cancelled
            if (!result.isPresent)
            {
                return null
            }

            // try to parse the input
            try
            {
                return parseInput(result.get())
            }

            // continue if parsing fails
            catch (ex:Exception)
            {
                Alert(Alert.AlertType.ERROR).apply()
                {
                    title = dialogTitle
                    headerText = "Failed to parse text input."
                    contentText = ex.message
                    showAndWait()
                }
                continue
            }
        }
    }
}
