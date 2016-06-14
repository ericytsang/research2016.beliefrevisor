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
import research2016.propositionallogic.And
import research2016.propositionallogic.Proposition
import research2016.propositionallogic.makeFrom
import research2016.propositionallogic.models
import research2016.propositionallogic.toDnf
import research2016.propositionallogic.toFullDnf

/**
 * the belief state output panel is a layout that displays a [List] of
 * [Proposition]s. this [List] can be specified by setting the [propositions]
 * field. the proposition is displayed in a [propositionsListView] in a format
 * specified by the [displayModeComboBox].
 */
class BeliefStateOutputPanel(labelText:String):VBox()
{
    companion object
    {
        /**
         * [List] of [DisplayModeOption]s used in the [displayModeComboBox] control.
         */
        private val displayModeOptions:List<DisplayModeOption> = run()
        {
            val cnfOption = DisplayModeOption("Conjunctive Normal Form",{it.map {it.toDnf()}})
            val fullDnfOption = DisplayModeOption("Full Disjunctive Normal Form",{it.map {it.toFullDnf()}})
            val defaultOption = DisplayModeOption("Default",{it})
            val modelsOption = DisplayModeOption("Models",{if (it.isNotEmpty()) And.make(it).models.map {Proposition.makeFrom(it)} else emptyList()})
            return@run listOf(defaultOption,modelsOption,cnfOption,fullDnfOption)
        }
    }

    /**
     * all elements of [observers] are notified of specific events when they
     * occur.
     */
    var observers = mutableSetOf<Observer>()

    /**
     * [propositions] are displayed in the [propositionsListView].
     */
    var propositions:List<Proposition> = emptyList()

        set(value)
        {
            field = value
            updateDisplay()
            observers.forEach {it.onItemsChanged()}
        }

    /**
     * elements of [observers] must implement this interface...
     */
    open class Observer
    {
        /**
         * called when a [Proposition] in the [propositionsListView] is
         * double-clicked.
         */
        open fun onPropositionDoubleClicked(proposition:Proposition) {}

        /**
         * called each time after [propositions] is set to a new value.
         */
        open fun onItemsChanged() {}
    }

    /**
     * [label] appears above the [propositionsListView].
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
    val propositionsListView = ListView<Proposition>()
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
                    observers.forEach {it.onPropositionDoubleClicked(focusModel.focusedItem)}
                }
            }
        }

    init
    {
        // add children to layout...
        children.addAll(label,propositionsListView,displayModeComboBox)

        // configure layout...
        spacing = Dimens.KEYLINE_SMALL.toDouble()
        setVgrow(propositionsListView,Priority.ALWAYS)
    }

    /**
     * computes how [Proposition]s should be displayed based on the current
     * [propositions] and the selected [DisplayModeOption].
     */
    private fun updateDisplay()
    {
        val transform = displayModeComboBox.value.transform
        val displayedPropositions = transform(propositions)
        Platform.runLater()
        {
            propositionsListView.items.clear()
            propositionsListView.items.addAll(displayedPropositions)
        }
    }

    /**
     * [name] is displayed directly in the [displayModeComboBox] control.
     * [transform] is used to convert each element in [propositions] into
     * another to be displayed in the [propositionsListView].
     */
    private class DisplayModeOption(val name:String,val transform:(List<Proposition>)->List<Proposition>)
    {
        override fun toString():String = name
    }
}
