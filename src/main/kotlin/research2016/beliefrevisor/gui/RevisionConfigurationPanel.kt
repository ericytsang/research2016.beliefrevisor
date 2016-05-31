package research2016.beliefrevisor.gui

import com.sun.javafx.collections.ObservableListWrapper
import javafx.beans.InvalidationListener
import javafx.scene.Node
import javafx.scene.control.ComboBox
import javafx.scene.layout.VBox
import research2016.beliefrevisor.core.HammingDistanceComparator
import research2016.beliefrevisor.core.OrderedSetsComparator
import research2016.beliefrevisor.core.SetInclusionComparator
import research2016.beliefrevisor.core.WeightedHammingDistanceComparator
import research2016.propositionallogic.BasicProposition
import research2016.propositionallogic.Proposition
import research2016.propositionallogic.Situation
import research2016.propositionallogic.makeFrom
import research2016.propositionallogic.toParsableString
import java.io.Serializable
import java.util.Comparator

class RevisionConfigurationPanel:VBox()
{
    companion object
    {
        const val SAVE_MAP_WEIGHTED_HAMMING_DISTANCE = "SAVE_MAP_WEIGHTED_HAMMING_DISTANCE"
        const val SAVE_MAP_SET_INCLUSION = "SAVE_MAP_SET_INCLUSION"
        const val SAVE_MAP_ORDERED_SETS = "SAVE_MAP_ORDERED_SETS"
    }

    private val hammingDistanceRevisionOperatorOption = HammingDistanceRevisionOperatorOption()
    private val weightedHammingDistanceRevisionOperatorOption = WeightedHammingDistanceRevisionOperatorOption()
    private val setInclusionRevisionOperatorOption = SetInclusionRevisionOperatorOption()
    private val orderedSetsRevisionOperatorOption = OrderedSetsRevisionOperatorOption()

    /**
     * [List] of [RevisionOperatorOption]s used in the
     * [revisionOperatorComboBox] control.
     */
    private val revisionOperatorOptions:List<RevisionOperatorOption> = run()
    {
        return@run listOf(hammingDistanceRevisionOperatorOption,
            weightedHammingDistanceRevisionOperatorOption,
            setInclusionRevisionOperatorOption,
            orderedSetsRevisionOperatorOption)
    }

    /**
     * returns a [Comparator] that can be used for belief revision.
     */
    fun situationComparator(initialBeliefState:Set<Proposition>):Comparator<Situation>
    {
        return revisionOperatorComboBox.value.situationComparator(initialBeliefState)
    }

    fun saveToMap():Map<String,Any>
    {
        val saveMap = mutableMapOf<String,Any>()
        saveMap.put(SAVE_MAP_WEIGHTED_HAMMING_DISTANCE,weightedHammingDistanceRevisionOperatorOption.operatorSettings.listView.items.toList())
        saveMap.put(SAVE_MAP_SET_INCLUSION,setInclusionRevisionOperatorOption.operatorSettings.listView.items.toList())
        saveMap.put(SAVE_MAP_ORDERED_SETS,orderedSetsRevisionOperatorOption.operatorSettings.listView.items.toList())
        return saveMap
    }

    @Suppress("UNCHECKED_CAST")
    fun loadFromMap(saveMap:Map<String,Any>)
    {
        weightedHammingDistanceRevisionOperatorOption.operatorSettings.listView.items = ObservableListWrapper(saveMap[SAVE_MAP_WEIGHTED_HAMMING_DISTANCE] as List<WeightedHammingDistanceRevisionOperatorOption.Mapping>? ?: emptyList())
        setInclusionRevisionOperatorOption.operatorSettings.listView.items = ObservableListWrapper(saveMap[SAVE_MAP_SET_INCLUSION] as List<Proposition>? ?: emptyList())
        orderedSetsRevisionOperatorOption.operatorSettings.listView.items = ObservableListWrapper(saveMap[SAVE_MAP_ORDERED_SETS] as List<Proposition>? ?: emptyList())
    }

    /**
     * [revisionOperatorComboBox] is used by the user to select which revision
     * operator they would like to use.
     */
    private val revisionOperatorComboBox = ComboBox<RevisionOperatorOption>()
        .apply()
        {
            valueProperty().addListener(InvalidationListener()
            {
                if (1 in this@RevisionConfigurationPanel.children.indices)
                {
                    this@RevisionConfigurationPanel.children.removeAt(1)
                }
                value.operatorSettings?.let()
                {
                    this@RevisionConfigurationPanel.children.add(it)
                }
            })
            items = ObservableListWrapper(revisionOperatorOptions)
            value = items.first()
        }

    init
    {
        spacing = Dimens.KEYLINE_SMALL.toDouble()
        children.addAll(revisionOperatorComboBox)
    }

    private abstract class RevisionOperatorOption(val name:String)
    {
        abstract val operatorSettings:Node?
        abstract fun situationComparator(initialBeliefState:Set<Proposition>):Comparator<Situation>
        override fun toString():String = name
    }

    private class  HammingDistanceRevisionOperatorOption:RevisionOperatorOption("Hamming Distance")
    {
        override val operatorSettings:Node? = null
        override fun situationComparator(initialBeliefState:Set<Proposition>):Comparator<Situation>
        {
            return HammingDistanceComparator(initialBeliefState)
        }
    }

    private class WeightedHammingDistanceRevisionOperatorOption:RevisionOperatorOption("Weighted Hamming Distance")
    {

        override val operatorSettings = object:EditableListView<Mapping>("mapping")
        {
            init
            {
                listView.minHeight = 100.0
                listView.prefHeight = listView.minHeight
            }

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

        override fun situationComparator(initialBeliefState:Set<Proposition>):Comparator<Situation>
        {
            val weights = operatorSettings.listView.items
                .associate {BasicProposition.make(it.variableName) to it.weight}
            return WeightedHammingDistanceComparator(initialBeliefState,weights)
        }

        class Mapping(val variableName:String,val weight:Int):Serializable
        {
            override fun toString():String = "$variableName = $weight"
        }
    }

    private class SetInclusionRevisionOperatorOption:RevisionOperatorOption("Set Inclusion")
    {
        override val operatorSettings = object:EditableListView<Proposition>("sentence")
        {
            init
            {
                listView.minHeight = 100.0
                listView.prefHeight = listView.minHeight
            }

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

        override fun situationComparator(initialBeliefState:Set<Proposition>):Comparator<Situation>
        {
            return SetInclusionComparator(operatorSettings.listView.items.toSet())
        }
    }

    private class OrderedSetsRevisionOperatorOption:RevisionOperatorOption("Ordered Sets")
    {
        override val operatorSettings = object:EditableListView<Proposition>("sentence")
        {
            init
            {
                listView.minHeight = 100.0
                listView.prefHeight = listView.minHeight
            }

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

        override fun situationComparator(initialBeliefState:Set<Proposition>):Comparator<Situation>
        {
            return OrderedSetsComparator(operatorSettings.listView.items)
        }
    }
}
