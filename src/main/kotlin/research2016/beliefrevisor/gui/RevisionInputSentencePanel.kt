package research2016.beliefrevisor.gui

import javafx.scene.control.Label
import javafx.scene.control.TextInputDialog
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import research2016.propositionallogic.Proposition
import research2016.propositionallogic.makeFrom
import research2016.propositionallogic.toParsableString
import java.util.Optional

/**
 * Created by surpl on 6/14/2016.
 */
class RevisionInputSentencePanel:VBox()
{
    companion object
    {
        const val LABEL_TEXT = "Sentences for Revision"
    }

    val listView = object:EditableListView<DisplayDecorator,TextInputDialog,String>()
    {
        override fun isInputCancelled(result:Optional<String>):Boolean
        {
            return !result.isPresent
        }

        override fun tryParseInput(inputDialog:TextInputDialog):DisplayDecorator
        {
            return DisplayDecorator(Proposition.makeFrom(inputDialog.result))
        }

        override fun makeInputDialog(model:DisplayDecorator?):TextInputDialog
        {
            return TextInputDialog(model?.proposition?.toParsableString())
                .apply {headerText = "Enter the sentence below."}
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

    inner class DisplayDecorator(val proposition:Proposition)
    {
        override fun toString():String
        {
            return displayModeComboBox.value.transform(listOf(proposition)).joinToString("\n")
        }
    }

    init
    {
        spacing = Dimens.KEYLINE_SMALL.toDouble()
        children.addAll(Label(LABEL_TEXT),listView,displayModeComboBox)
        VBox.setVgrow(listView,Priority.ALWAYS)
    }
}
