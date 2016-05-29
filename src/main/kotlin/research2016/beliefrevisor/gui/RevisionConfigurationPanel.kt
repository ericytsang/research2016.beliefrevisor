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
import research2016.propositionallogic.Proposition
import research2016.propositionallogic.Situation
import research2016.propositionallogic.makeFrom
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
        override val operatorSettings:Node? = Region()
        override fun situationComparator(initialBeliefState:Set<Proposition>):Comparator<Situation>
        {
            // todo
            throw UnsupportedOperationException()
        }
    }

    private class SetInclusionRevisionOperatorOption:RevisionOperatorOption("Set Inclusion")
    {
        private val propositionList = ListView<Proposition>().apply()
        {
            // todo magic number
            prefHeight = 100.0

            // allow editing of sentences by double-clicking on them
            onMouseClicked = EventHandler()
            {
                event ->

                // continue if it is a double click, and an item in the list is selected
                if (!(event.clickCount == 2 && focusModel.focusedItem != null))
                {
                    return@EventHandler
                }

                var previousResult:String? = null
                while (true)
                {
                    // show text input dialog to get sentence from user
                    val result = TextInputDialog(previousResult ?: focusModel.focusedItem!!.toString())
                        .apply()
                        {
                            title = "Edit Existing Sentence"
                            headerText = "Please enter the sentence below."
                        }
                        .showAndWait()

                    // try to update the existing sentence...
                    try
                    {
                        if (result.isPresent)
                        {
                            items[focusModel.focusedIndex] = Proposition.makeFrom(result.get())
                        }
                        break
                    }
                    catch (ex:Exception)
                    {
                        previousResult = result.get()

                        // sentence format is probably invalid
                        val alert = Alert(Alert.AlertType.ERROR)
                        alert.title = "Parsing Error"
                        alert.headerText = "Invalid sentence format."
                        alert.contentText = ex.message
                        alert.showAndWait()
                    }
                }
            }

            // allow deletion of sentences by pressing delete or bacspace
            onKeyReleased = EventHandler()
            {
                event ->
                if (event.code in setOf(KeyCode.DELETE,KeyCode.BACK_SPACE))
                {
                    items.removeAt(focusModel.focusedIndex)
                }
            }
        }

        /**
         * [Button] that when clicked, displays a text input dialog to get a
         * sentence from the user, which will then be added to the
         * [propositionList].
         */
        private val addSentenceButton = Button("Add Sentence").apply()
        {
            onAction = EventHandler()
            {
                var previousResult:String? = null
                while (true)
                {
                    // show text input dialog to get sentence from user
                    val result = TextInputDialog(previousResult ?: "")
                        .apply()
                        {
                            title = "Add New Sentence"
                            headerText = "Please enter the sentence below."
                        }
                        .showAndWait()

                    // try to add the new sentence...
                    try
                    {
                        if (result.isPresent)
                        {
                            propositionList.items.add(Proposition.makeFrom(result.get()))
                        }
                        break
                    }
                    catch (ex:Exception)
                    {
                        previousResult = result.get()

                        // sentence format is probably invalid
                        val alert = Alert(Alert.AlertType.ERROR)
                        alert.title = "Parsing Error"
                        alert.headerText = "Invalid sentence format."
                        alert.contentText = ex.message
                        alert.showAndWait()
                    }
                }
            }
        }

        override val operatorSettings:Node? = VBox().apply()
        {
            spacing = Dimens.KEYLINE_SMALL.toDouble()
            children.addAll(propositionList,addSentenceButton)
        }

        override fun situationComparator(initialBeliefState:Set<Proposition>):Comparator<Situation>
        {
            return SetInclusionComparator(propositionList.items.toSet())
        }
    }
}
