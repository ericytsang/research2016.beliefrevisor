package research2016.beliefrevisor.gui

import com.sun.javafx.collections.ObservableListWrapper
import javafx.scene.Node
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import research2016.beliefrevisor.core.BeliefRevisionStrategy
import research2016.beliefrevisor.core.ComparatorBeliefRevisionStrategy
import research2016.beliefrevisor.core.HammingDistanceComparator
import research2016.beliefrevisor.core.OrderedSetsComparator
import research2016.beliefrevisor.core.SatisfiabilityBeliefRevisionStrategy
import research2016.beliefrevisor.core.WeightedHammingDistanceComparator
import research2016.propositionallogic.Proposition
import research2016.propositionallogic.Variable
import research2016.propositionallogic.makeFrom
import research2016.propositionallogic.toParsableString
import java.io.Serializable

class BeliefRevisionConfigurationPanel:VBox()
{
    companion object
    {
        const val LABEL_TEXT = "Belief Revision Strategy"
        const val SAVE_MAP_WEIGHTED_HAMMING_DISTANCE = "SAVE_MAP_WEIGHTED_HAMMING_DISTANCE"
        const val SAVE_MAP_ORDERED_SETS = "SAVE_MAP_ORDERED_SETS"
    }

    private val hammingDistanceRevisionOperatorOption = HammingDistanceOption()
    private val weightedHammingDistanceRevisionOperatorOption = WeightedHammingDistanceOption()
    private val setInclusionRevisionOperatorOption = SatisfiabilityOption()
    private val orderedSetsRevisionOperatorOption = OrderedSetsOption()

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
    val beliefRevisionStrategy:BeliefRevisionStrategy
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
        weightedHammingDistanceRevisionOperatorOption.settingsPanel.listView.items = ObservableListWrapper(saveMap[SAVE_MAP_WEIGHTED_HAMMING_DISTANCE] as List<WeightedHammingDistanceOption.Mapping>? ?: emptyList())
        orderedSetsRevisionOperatorOption.settingsPanel.listView.items = ObservableListWrapper(saveMap[SAVE_MAP_ORDERED_SETS] as List<Proposition>? ?: emptyList())
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
                    this@BeliefRevisionConfigurationPanel.children.remove(oldValue.settingsPanel)
                }
                newValue.settingsPanel?.let()
                {
                    this@BeliefRevisionConfigurationPanel.children.add(it)
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
        abstract val beliefRevisionStrategy:BeliefRevisionStrategy
        override fun toString():String = name
    }

    private class HammingDistanceOption:Option("Hamming Distance")
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

    private class WeightedHammingDistanceOption:Option("Weighted Hamming Distance")
    {

        override val settingsPanel = object:EditableListView<Mapping>("mapping")
        {
            override fun parse(string:String):Mapping
            {
                val subStrings = string.split("=")
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

            override fun toInputString(entry:Mapping):String
            {
                return entry.toString()
            }

            override fun addToList(existingEntries:MutableList<Mapping>,indexOfEntry:Int,newEntry:Mapping):String?
            {
                if (existingEntries.any {it.variableName == newEntry.variableName})
                {
                    return "A mapping for the variable \"${newEntry.variableName}\" already exists."
                }
                else
                {
                    existingEntries.add(indexOfEntry,newEntry)
                    return null
                }
            }

            override fun removeFromList(existingEntries:MutableList<Mapping>,indexOfEntry:Int):String?
            {
                existingEntries.removeAt(indexOfEntry)
                return null
            }

            override fun updateListAt(existingEntries:MutableList<Mapping>,indexOfEntry:Int,newEntry:Mapping):String?
            {
                val resultingList = existingEntries.filterIndexed {i,e -> i != indexOfEntry}
                if (resultingList.any {it.variableName == newEntry.variableName})
                {
                    return "A mapping for the variable \"${newEntry.variableName}\" already exists."
                }
                else
                {
                    existingEntries[indexOfEntry] = newEntry
                    return null
                }
            }
        }

        override val beliefRevisionStrategy:BeliefRevisionStrategy
            get()
            {
                return ComparatorBeliefRevisionStrategy()
                {
                    initialBeliefState:Set<Proposition> ->
                    val weights = settingsPanel.listView.items.associate {Variable.make(it.variableName) to it.weight}
                    WeightedHammingDistanceComparator(initialBeliefState,weights)
                }
            }

        class Mapping(val variableName:String,val weight:Int):Serializable
        {
            override fun toString():String = "$variableName = $weight"
        }
    }

    private class SatisfiabilityOption:Option("Satisfiability")
    {
        override val settingsPanel = null
        override val beliefRevisionStrategy:BeliefRevisionStrategy = SatisfiabilityBeliefRevisionStrategy()
    }

    private class OrderedSetsOption:Option("Ordered Sets")
    {
        override val settingsPanel = object:EditableListView<Proposition>("sentence")
        {
            override fun parse(string:String):Proposition
            {
                return Proposition.makeFrom(string)
            }

            override fun toInputString(entry:Proposition):String
            {
                return entry.toParsableString()
            }

            override fun addToList(existingEntries:MutableList<Proposition>,indexOfEntry:Int,newEntry:Proposition):String?
            {
                existingEntries.add(indexOfEntry,newEntry)
                return null
            }

            override fun removeFromList(existingEntries:MutableList<Proposition>,indexOfEntry:Int):String?
            {
                existingEntries.removeAt(indexOfEntry)
                return null
            }

            override fun updateListAt(existingEntries:MutableList<Proposition>,indexOfEntry:Int,newEntry:Proposition):String?
            {
                existingEntries[indexOfEntry] = newEntry
                return null
            }
        }

        override val beliefRevisionStrategy:BeliefRevisionStrategy
            get()
            {
                return ComparatorBeliefRevisionStrategy()
                {
                    initialBeliefState:Set<Proposition> ->
                    OrderedSetsComparator(initialBeliefState,settingsPanel.listView.items)
                }
            }
    }
}
