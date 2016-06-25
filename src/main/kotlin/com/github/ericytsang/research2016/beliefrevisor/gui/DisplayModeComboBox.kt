package com.github.ericytsang.research2016.beliefrevisor.gui

import com.sun.javafx.collections.ObservableListWrapper
import javafx.scene.control.ComboBox
import com.github.ericytsang.research2016.propositionallogic.And
import com.github.ericytsang.research2016.propositionallogic.Proposition
import com.github.ericytsang.research2016.propositionallogic.models
import com.github.ericytsang.research2016.propositionallogic.toDnf
import com.github.ericytsang.research2016.propositionallogic.toFullDnf

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
            val cnfOption = Option("Disjunctive Normal Form",{it.map {it.toDnf().toString()}})
            val fullDnfOption = Option("Full Disjunctive Normal Form",{it.map {it.toFullDnf().toString()}})
            val defaultOption = Option("Default",{it.map {it.toString()}})
            val modelsOption = Option("Models",{if (it.isNotEmpty()) And.make(it).models.map {it.toString()} else emptyList()})
            return@run listOf(defaultOption,modelsOption,cnfOption,fullDnfOption)
        }
    }

    /**
     * [name] is displayed directly in the [displayModeComboBox] control.
     * [transform] is unused within this class.
     */
    class Option(val name:String,val transform:(List<Proposition>)->List<String>)
    {
        override fun toString():String = name
    }

    init
    {
        items = ObservableListWrapper(options)
        valueProperty().set(items.first())
    }
}
