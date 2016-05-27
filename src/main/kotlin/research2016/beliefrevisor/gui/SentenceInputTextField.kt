package research2016.beliefrevisor.gui

import javafx.beans.InvalidationListener
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import research2016.propositionallogic.Proposition
import research2016.propositionallogic.makeFrom

/**
 * Created by surpl on 5/25/2016.
 */
class InputPane:VBox()
{
    companion object
    {
        const val SENTENCE_INPUT_TEXT_FIELD_PROMPT = "Type sentence here e.g. \"a and (b or -c)\""

        /**
         * displayed when the text in the [sentenceTextField] could not be parsed.
         */
        const val LABEL_ERROR_PROMPT = "ERROR"

        /**
         * displayed when there is no text in the [sentenceTextField].
         */
        const val LABEL_NO_TEXT_PROMPT = "No text entered."

        /**
         * displayed when the text in the [sentenceTextField] can be parsed.
         */
        const val LABEL_SENTENCE_PROMPT = "Sentence"
    }

    /**
     * text field that the user types their sentence into.
     */
    val sentenceTextField = SentenceInputTextField()

    /**
     * label used to display any parsing errors if any.
     */
    val errorLabel = Label(LABEL_NO_TEXT_PROMPT)

    init
    {
        // add all controls to the root layout of this instance
        children.addAll(sentenceTextField,errorLabel)

        // configure controls...
        sentenceTextField.promptText = SENTENCE_INPUT_TEXT_FIELD_PROMPT

        // add listeners to the sentenceTextField to update the error message
        // text field when needed.
        sentenceTextField.textProperty().addListener(InvalidationListener()
        {
            errorLabel.text =
                sentenceTextField.errorMessage?.let {"$LABEL_ERROR_PROMPT: $it"}
                ?: sentenceTextField.sentence?.let {"$LABEL_SENTENCE_PROMPT: $it"}
                ?: LABEL_NO_TEXT_PROMPT
        })
    }
}

/**
 * [TextField]. when the text within it changes, it attempts to parse the [text]
 * into a [Proposition], and stores a reference to it in [sentence]. if an
 * exception occurs while parsing the [text], then the error description is
 * stored in [errorMessage].
 */
class SentenceInputTextField:TextField()
{
    /**
     * brief description of any parsing error that occurs; null if there were no
     * parsing errors.
     */
    var errorMessage:String? = null

    /**
     * the sentence that was entered into this text field.
     */
    var sentence:Proposition? = null

    init
    {
        // every time the text property changes...
        textProperty().addListener(InvalidationListener()
        {
            // reset values
            errorMessage = null
            sentence = null
            styleClass.remove(CSS.WARNING_CONTROL)

            // parse the inputted text and populate values
            try
            {
                if (text.isNotBlank())
                {
                    sentence = Proposition.makeFrom(text)
                }
            }
            catch (ex:Exception)
            {
                styleClass.add(CSS.WARNING_CONTROL)
                errorMessage = ex.message
            }
        })
    }
}
