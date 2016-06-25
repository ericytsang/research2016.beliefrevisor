package com.github.ericytsang.research2016.beliefrevisor.gui

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import javafx.scene.control.TextInputDialog
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import com.github.ericytsang.research2016.propositionallogic.Proposition
import com.github.ericytsang.research2016.propositionallogic.makeFrom
import com.github.ericytsang.research2016.propositionallogic.toParsableString
import java.util.Optional

/**
 * Created by surpl on 6/14/2016.
 */
class RevisionInputBeliefStatePanel:VBox()
{
    companion object
    {
        const val LABEL_TEXT = "Initial Belief State"
    }

    val listView = object:EditableListView<DisplayDecorator,Alert,ButtonType>()
    {
        override fun tryParseInput(inputDialog:Alert):DisplayDecorator
        {
            @Suppress("UNCHECKED_CAST")
            val beliefState = (inputDialog.dialogPane.content as EditableListView<Proposition,TextInputDialog,String>)
                .listView.items.toSet().let {DisplayDecorator(it)}
            if (beliefState.propositions.isEmpty())
            {
                throw IllegalArgumentException("A belief state must consist of at least one sentence.")
            }
            else
            {
                return beliefState
            }
        }

        override fun makeInputDialog(model:DisplayDecorator?):Alert
        {
            return Alert(Alert.AlertType.NONE).apply()
            {
                headerText = "Editing belief state."
                buttonTypes.addAll(ButtonType.CANCEL,ButtonType.OK)
                dialogPane.content = makePropositionListView().apply()
                {
                    if (model?.propositions?.isNotEmpty() == true)
                    {
                        listView.items.addAll(model!!.propositions)
                    }
                }
                onShown = EventHandler()
                {
                    Platform.runLater()
                    {
                        (dialogPane.content as EditableListView<*,*,*>).listView.requestFocus()
                    }
                }
            }
        }

        override fun isInputCancelled(result:Optional<ButtonType>):Boolean
        {
            return result.get() in setOf(ButtonType.CANCEL,ButtonType.CLOSE)
        }
    }

    private val displayModeComboBox:DisplayModeComboBox = DisplayModeComboBox().apply()
    {
        valueProperty().addListener()
        {
            observable ->
            listView.listView.refresh()
        }
    }

    inner class DisplayDecorator(val propositions:Set<Proposition>)
    {
        override fun toString():String
        {
            return displayModeComboBox.value.transform(propositions.toList()).joinToString("\n")
        }
    }

    init
    {
        spacing = Dimens.KEYLINE_SMALL.toDouble()
        children.addAll(Label(LABEL_TEXT),listView,displayModeComboBox)
        setVgrow(listView,Priority.ALWAYS)
    }

    private fun makePropositionListView() = object:EditableListView<Proposition,TextInputDialog,String>()
    {
        override fun isInputCancelled(result:Optional<String>):Boolean
        {
            return !result.isPresent
        }

        override fun tryParseInput(inputDialog:TextInputDialog):Proposition
        {
            return Proposition.makeFrom(inputDialog.result)
        }

        override fun makeInputDialog(model:Proposition?):TextInputDialog
        {
            return TextInputDialog(model?.toParsableString())
                .apply {headerText = "Enter the sentence below."}
        }
    }
}
