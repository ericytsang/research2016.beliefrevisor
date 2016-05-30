package research2016.beliefrevisor.gui

import com.sun.javafx.collections.ObservableListWrapper
import javafx.beans.InvalidationListener
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.ListView
import javafx.scene.control.TextInputDialog
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import research2016.beliefrevisor.core.HammingDistanceComparator
import research2016.beliefrevisor.core.SetInclusionComparator
import research2016.beliefrevisor.core.WeightedHammingDistanceComparator
import research2016.propositionallogic.BasicProposition
import research2016.propositionallogic.Proposition
import research2016.propositionallogic.Situation
import research2016.propositionallogic.makeFrom
import research2016.propositionallogic.toParsableString
import java.util.Comparator
import java.util.Optional

class RevisionConfigurationPanel:VBox()
{
    companion object
    {
        /**
         * [List] of [RevisionOperatorOption]s used in the
         * [revisionOperatorComboBox] control.
         */
        private val revisionOperatorOptions:List<RevisionOperatorOption> = run()
        {
            return@run listOf(HammingDistanceRevisionOperatorOption(),
                WeightedHammingDistanceRevisionOperatorOption(),
                SetInclusionRevisionOperatorOption())
        }
    }

    /**
     * returns a [Comparator] that can be used for belief revision.
     */
    fun situationComparator(initialBeliefState:Set<Proposition>):Comparator<Situation>
    {
        return revisionOperatorComboBox.value.situationComparator(initialBeliefState)
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

    private class HammingDistanceRevisionOperatorOption:RevisionOperatorOption("Hamming Distance")
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
                listView.prefHeight = 100.0
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

        class Mapping(val variableName:String,val weight:Int)
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
                listView.prefHeight = 100.0
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
}
