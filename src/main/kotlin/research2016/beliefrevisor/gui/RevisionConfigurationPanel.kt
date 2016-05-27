package research2016.beliefrevisor.gui

import com.sun.javafx.collections.ObservableListWrapper
import javafx.beans.InvalidationListener
import javafx.scene.Node
import javafx.scene.control.ComboBox
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import research2016.beliefrevisor.core.HammingDistanceComparator
import research2016.propositionallogic.Proposition
import research2016.propositionallogic.Situation
import java.util.Comparator

class RevisionConfigurationPanel:HBox()
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
     * [revisionOperatorSettings] is a layout used to display the settings for
     * the revision operator selected in the [revisionOperatorComboBox]. these
     * settings are configurable by the user.
     */
    private val revisionOperatorSettings = StackPane()

    /**
     * [revisionOperatorComboBox] is used by the user to select which revision
     * operator they would like to use.
     */
    private val revisionOperatorComboBox = ComboBox<RevisionOperatorOption>()
        .apply()
        {
            valueProperty().addListener(InvalidationListener()
            {
                revisionOperatorSettings.children.clear()
                revisionOperatorSettings.children.add(value.operatorSettings)
            })
            items = ObservableListWrapper(revisionOperatorOptions)
            value = HammingDistanceRevisionOperatorOption()
        }

    init
    {
        children.addAll(revisionOperatorComboBox,revisionOperatorSettings)
    }

    private abstract class RevisionOperatorOption(val name:String)
    {
        abstract val operatorSettings:Node
        abstract fun situationComparator(initialBeliefState:Set<Proposition>):Comparator<Situation>
        override fun toString():String = name
    }

    private class HammingDistanceRevisionOperatorOption:RevisionOperatorOption("Hamming Distance")
    {
        override val operatorSettings:Node = Region()
        override fun situationComparator(initialBeliefState:Set<Proposition>):Comparator<Situation>
        {
            return HammingDistanceComparator(initialBeliefState)
        }
    }

    private class WeightedHammingDistanceRevisionOperatorOption:RevisionOperatorOption("Weighted Hamming Distance")
    {
        override val operatorSettings:Node = Region()
        override fun situationComparator(initialBeliefState:Set<Proposition>):Comparator<Situation>
        {
            // todo
            throw UnsupportedOperationException()
        }
    }

    private class SetInclusionRevisionOperatorOption:RevisionOperatorOption("Set Inclusion")
    {
        override val operatorSettings:Node = Region()
        override fun situationComparator(initialBeliefState:Set<Proposition>):Comparator<Situation>
        {
            // todo
            throw UnsupportedOperationException()
        }
    }
}
