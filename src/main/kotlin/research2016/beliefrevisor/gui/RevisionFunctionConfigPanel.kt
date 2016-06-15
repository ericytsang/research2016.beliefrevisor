package research2016.beliefrevisor.gui

import com.sun.javafx.collections.ObservableListWrapper
import javafx.beans.InvalidationListener
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextInputDialog
import javafx.scene.layout.VBox
import lib.collections.getRandom
import research2016.beliefrevisor.core.BeliefRevisionStrategy
import research2016.beliefrevisor.core.ComparatorBeliefRevisionStrategy
import research2016.beliefrevisor.core.HammingDistanceComparator
import research2016.beliefrevisor.core.OrderedSetsComparator
import research2016.beliefrevisor.core.SatisfiabilityBeliefRevisionStrategy
import research2016.beliefrevisor.core.WeightedHammingDistanceComparator
import research2016.propositionallogic.Or
import research2016.propositionallogic.Proposition
import research2016.propositionallogic.State
import research2016.propositionallogic.Variable
import research2016.propositionallogic.generateFrom
import research2016.propositionallogic.makeFrom
import research2016.propositionallogic.toParsableString
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
        saveMap.put(SAVE_MAP_WEIGHTED_HAMMING_DISTANCE,weightedHammingDistanceRevisionOperatorOption.settingsPanel.listView.items.toList())
        saveMap.put(SAVE_MAP_ORDERED_SETS,orderedSetsRevisionOperatorOption.settingsPanel.listView.items.toList())
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
                        .apply {headerText = "Enter the sentence below."}
                }
            }
            val makeRandomButton = Button(BUTTON_MAKE_RANDOMORDERED_SET).apply()
            {
                onAction = EventHandler()
                {
                    val variablesTextInputDialog = TextInputDialog()
                        .apply()
                        {
                            title = "Generate Random Ordering"
                            headerText = "Enter a comma separated list of all the variables below."
                        }
                    val numBucketsTextInputDialog = TextInputDialog()
                        .apply()
                        {
                            title = "Generate Random Ordering"
                            headerText = "Enter the number of buckets to sort generated states into."
                        }

                    var variables:Set<Variable>? = null
                    var numBuckets:Int? = null

                    while (true)
                    {
                        // get the variables input
                        val variablesTextInput = variablesTextInputDialog.showAndWait()

                        // abort if the operation is cancelled
                        if (!variablesTextInput.isPresent)
                        {
                            return@EventHandler
                        }

                        // try to parse the input
                        try
                        {
                            variables = variablesTextInput.get().split(",").map {Variable.make(it.trim())}.toSet()
                            break
                        }

                        // continue if parsing fails
                        catch (ex:Exception)
                        {
                            val error = Alert(Alert.AlertType.ERROR)
                            error.title = "Generate Random Ordering"
                            error.headerText = "Failed to parse text input"
                            error.contentText = ex.message
                            error.showAndWait()
                            continue
                        }
                    }

                    while (true)
                    {
                        // get the num buckets input
                        val numBucketsTextInput = numBucketsTextInputDialog.showAndWait()

                        // abort if the operation is cancelled
                        if (!numBucketsTextInput.isPresent)
                        {
                            return@EventHandler
                        }

                        // try to parse the input
                        try
                        {
                            numBuckets = numBucketsTextInput.get().toInt()
                            break
                        }

                        // continue if parsing fails
                        catch (ex:Exception)
                        {
                            val error = Alert(Alert.AlertType.ERROR)
                            error.title = "Generate Random Ordering"
                            error.headerText = "Failed to parse text input"
                            error.contentText = ex.message
                            error.showAndWait()
                            continue
                        }
                    }

                    val allStates = State.generateFrom(variables!!)
                        .toMutableList()
                        .apply {Collections.shuffle(this)}
                    val buckets = Array<MutableSet<State>>(numBuckets!!,{mutableSetOf()})
                    allStates.forEach {buckets.getRandom().add(it)}
                    listview.listView.items.clear()
                    listview.listView.items.addAll(buckets.filter {it.isNotEmpty()}.map {Or.make(it.map {Proposition.makeFrom(it)})})
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
}
