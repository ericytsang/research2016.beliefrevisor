package com.github.ericytsang.research2016.beliefrevisor.gui

import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import com.github.ericytsang.research2016.propositionallogic.Proposition

/**
 * the belief state output panel is a layout that displays a [List] of
 * [Proposition]s. this [List] can be specified by setting the [propositions]
 * field. the proposition is displayed in a [listView] in a format
 * specified by the [displayModeComboBox].
 */
class RevisionOutputResultPanel():VBox()
{
    companion object
    {
        const val LABEL_TEXT = "Resulting Belief State"
    }

    /**
     * [ListView] used to display all the [propositions] in the format specified
     * by the [DisplayModeOption] mode selected in the [displayModeComboBox]
     * control.
     */
    val listView = ListView<DisplayDecorator>()

    /**
     * [ComboBox] for user to select how [propositions] should be displayed.
     */
    private val displayModeComboBox = DisplayModeComboBox()
        .apply()
        {
            valueProperty().addListener {o -> listView.refresh()}
        }

    inner class DisplayDecorator(val propositions:List<Proposition>)
    {
        override fun toString():String
        {
            return displayModeComboBox.value.transform(propositions).joinToString("\n")
        }
    }

    init
    {
        spacing = Dimens.KEYLINE_SMALL.toDouble()
        children.addAll(Label(LABEL_TEXT),listView,displayModeComboBox)
        setVgrow(listView,Priority.ALWAYS)
    }
}
