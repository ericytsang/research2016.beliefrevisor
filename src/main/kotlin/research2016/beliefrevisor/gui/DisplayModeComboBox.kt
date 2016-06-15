package research2016.beliefrevisor.gui

import com.sun.javafx.collections.ObservableListWrapper
import javafx.scene.control.ComboBox
import research2016.propositionallogic.And
import research2016.propositionallogic.Proposition
import research2016.propositionallogic.makeFrom
import research2016.propositionallogic.models
import research2016.propositionallogic.toDnf
import research2016.propositionallogic.toFullDnf

/**
 * Created by surpl on 6/14/2016.
 */
class DisplayModeComboBox:ComboBox<DisplayModeComboBox.Option>()
{
    companion object
    {
        /**
         * [List] of [Option]s used in the [displayModeComboBox] control.
         */
        private val options:List<Option> = run()
        {
            val cnfOption = Option("Conjunctive Normal Form",{it.map {it.toDnf()}})
            val fullDnfOption = Option("Full Disjunctive Normal Form",{it.map {it.toFullDnf()}})
            val defaultOption = Option("Default",{it})
            val modelsOption = Option("Models",{if (it.isNotEmpty()) And.make(it).models.map {Proposition.makeFrom(it)} else emptyList()})
            return@run listOf(defaultOption,modelsOption,cnfOption,fullDnfOption)
        }
    }

    /**
     * [name] is displayed directly in the [displayModeComboBox] control.
     * [transform] is unused within this class.
     */
    class Option(val name:String,val transform:(List<Proposition>)->List<Proposition>)
    {
        override fun toString():String = name
    }

    init
    {
        items = ObservableListWrapper(options)
        valueProperty().set(items.first())
    }
}
