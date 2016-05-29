package research2016.beliefrevisor.gui

import com.sun.javafx.collections.ObservableListWrapper
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.event.EventHandler
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import research2016.propositionallogic.Contradiction
import research2016.propositionallogic.Proposition
import research2016.propositionallogic.Tautology
import research2016.propositionallogic.toDnf

class BeliefStateOutputPanel(labelText:String):VBox()
{
    companion object
    {
        /**
         * [List] of [DisplayModeOption]s used in the [displayModeComboBox] control.
         */
        private val displayModeOptions:List<DisplayModeOption> = run()
        {
            val cnfOption = DisplayModeOption("Conjunctive Normal Form",{proposition:Proposition -> proposition})
            val dnfOption = DisplayModeOption("Disjunctive Normal Form",{proposition:Proposition -> proposition.toDnf()})
            val defaultOption = DisplayModeOption("Default",{proposition:Proposition -> proposition})
            val simplifiedOption = DisplayModeOption("Simplified",{proposition:Proposition -> proposition})
            return@run listOf(defaultOption,simplifiedOption,cnfOption,dnfOption)
        }
    }

    /**
     * listener that is notified of events associated with this instance.
     */
    var listeners = mutableSetOf<Listener>()

    /**
     * [propositions] are displayed in the [beliefSetListView].
     */
    var propositions:List<Proposition> = emptyList()

        set(value)
        {
            field = value
            updateDisplay()
            listeners.forEach {it.onItemsChanged()}
        }

    open class Listener
    {
        open fun onPropositionDoubleClicked(proposition:Proposition) {}
        open fun onItemsChanged() {}
    }

    /**
     * [label] appears above the [beliefSetListView].
     */
    private val label = Label(labelText)

    /**
     * [ComboBox] for user to select how [propositions] should be displayed.
     */
    private val displayModeComboBox = ComboBox<DisplayModeOption>()
        .apply()
        {
            valueProperty().set(displayModeOptions.first())
            valueProperty().addListener(InvalidationListener()
            {
                updateDisplay()
            })
            items = ObservableListWrapper(displayModeOptions)
        }

    /**
     * [ListView] used to display all the [propositions] in the format specified
     * by the [DisplayModeOption] mode selected in the [displayModeComboBox]
     * control.
     */
    private val beliefSetListView = ListView<Proposition>()
        .apply()
        {
            // when delete or backspace key is pressed, remove selected element
            onKeyReleased = EventHandler()
            {
                event ->
                if (event.code in (setOf(KeyCode.DELETE,KeyCode.BACK_SPACE)) &&
                    focusModel.focusedItem != null)
                {
                    propositions = propositions.filterIndexed {i,e -> i != focusModel.focusedIndex}
                }
            }

            // when a proposition is double clicked, notify the listener
            onMouseClicked = EventHandler()
            {
                event ->
                if (event.clickCount == 2 && focusModel.focusedItem != null)
                {
                    listeners.forEach {it.onPropositionDoubleClicked(focusModel.focusedItem)}
                }
            }
        }

    init
    {
        // add children to layout...
        children.addAll(label,beliefSetListView,displayModeComboBox)

        // configure layout...
        spacing = Dimens.KEYLINE_SMALL.toDouble()
        setVgrow(beliefSetListView,Priority.ALWAYS)
    }

    /**
     * computes how [Proposition]s should be displayed based on the current
     * [propositions] and the selected [DisplayModeOption].
     */
    private fun updateDisplay()
    {
        val transform = displayModeComboBox.value.transform
        val displayedPropositions = propositions.map {transform(it)}
        Platform.runLater()
        {
            beliefSetListView.items.clear()
            beliefSetListView.items.addAll(displayedPropositions)
        }
    }

    /**
     * [name] is displayed directly in the [displayModeComboBox] control.
     * [transform] is used to convert each element in [propositions] into
     * another to be displayed in the [beliefSetListView].
     */
    private class DisplayModeOption(val name:String,val transform:(Proposition)->Proposition)
    {
        override fun toString():String = name
    }
}
