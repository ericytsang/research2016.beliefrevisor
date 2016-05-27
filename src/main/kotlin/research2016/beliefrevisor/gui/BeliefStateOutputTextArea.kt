package research2016.beliefrevisor.gui

import com.sun.javafx.collections.ObservableListWrapper
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import research2016.propositionallogic.Contradiction
import research2016.propositionallogic.Proposition
import research2016.propositionallogic.Tautology

class BeliefStateOutputPanel(labelText:String):VBox()
{
    companion object
    {
        /**
         * [List] of [DisplayMode]s used in the [displayModeSetting] control.
         */
        private val displayModeOptions:List<DisplayMode> = run()
        {
            val cnfOption = DisplayMode("Conjunctive Normal Form",{proposition:Proposition -> proposition})
            val dnfOption = DisplayMode("Disjunctive Normal Form",{proposition:Proposition -> proposition})
            val defaultOption = DisplayMode("Default",{proposition:Proposition -> Contradiction})
            val simplifiedOption = DisplayMode("Simplified",{proposition:Proposition -> Tautology})
            return@run listOf(defaultOption,simplifiedOption,cnfOption,dnfOption)
        }
    }

    /**
     * [propositions] are displayed in the [beliefSetTextArea].
     */
    var propositions:Set<Proposition> = emptySet()

        set(value)
        {
            field = value
            updateDisplay()
        }

    /**
     * [label] appears above the [beliefSetTextArea].
     */
    private val label = Label(labelText)

    /**
     * [ComboBox] for user to select how [propositions] should be displayed.
     */
    private val displayModeSetting = ComboBox<DisplayMode>()
        .apply()
        {
            valueProperty().addListener(InvalidationListener()
            {
                updateDisplay()
            })
            items = ObservableListWrapper(displayModeOptions)
            valueProperty().set(displayModeOptions.first())
        }

    /**
     * [TextArea] used to display all the [propositions] in the format specified
     * by the [DisplayMode] mode selected in the [displayModeSetting] control.
     */
    private val beliefSetTextArea = BeliefStateOutputTextArea()

    init
    {
        // add children to layout...
        children.addAll(label,beliefSetTextArea,displayModeSetting)

        // configure layout...
        spacing = Dimens.KEYLINE_SMALL.toDouble()
        setVgrow(beliefSetTextArea,Priority.ALWAYS)
    }

    /**
     * computes how [Proposition]s should be displayed based on the current
     * [propositions] and the selected [DisplayMode].
     */
    private fun updateDisplay()
    {
        val transform = displayModeSetting.value.transform
        val displayedPropositions = propositions.map {transform(it)}.toSet()
        Platform.runLater()
        {
            beliefSetTextArea.propositions = displayedPropositions
        }
    }

    /**
     * [name] is displayed directly in the [displayModeSetting] control.
     * [transform] is used to convert each element in [propositions] into
     * another to be displayed in the [beliefSetTextArea].
     */
    private class DisplayMode(val name:String,val transform:(Proposition)->Proposition)
    {
        override fun toString():String = name
    }
}

private class BeliefStateOutputTextArea:TextArea()
{
    init
    {
        isEditable = false
    }

    /**
     * each element of [propositions] is displayed in its own line in this
     * un-editable [TextArea]. whenever [propositions] is set, it updates the
     * [TextArea].
     */
    var propositions:Set<Proposition> = emptySet()

        // update text area text every time this value is set
        set(value)
        {
            field = value
            assert(Platform.isFxApplicationThread())
            text = propositions
                .fold(StringBuilder())
                {
                    stb,it -> stb.append("$it\n")
                }
                .toString()
        }
}
