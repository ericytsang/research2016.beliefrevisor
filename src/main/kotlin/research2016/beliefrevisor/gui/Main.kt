package research2016.beliefrevisor.gui

import javafx.application.Application
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Stage

fun main(args:Array<String>)
{
    Application.launch(Gui::class.java)
}

class Gui():Application()
{
    companion object
    {
        const val ADD_TO_BELIEF_STATE_BUTTON_TEXT = "Add to Initial Belief State"
        const val ADD_FOR_BELIEF_REVISION_BUTTON_TEXT = "Add for Belief Revision"
        const val PERFORM_REVISION_BUTTON_TEXT = "Revise"
        const val COMMIT_REVISION_BUTTON_TEXT = "Commit"
        const val INITIAL_BELIEF_STATE_LABEL_TEXT = "Initial Belief State"
        const val REVISION_SENTENCES_LABEL_TEXT = "Sentences for Revision"
        const val RESULTING_BELIEF_STATE_LABEL_TEXT = "Resulting Belief State"
    }

    val sentenceTextField = InputPane()

    /**
     * button that when clicked, indicates that the user wants to add the text
     * from [sentenceTextField] to belief state.
     */
    val addToBeliefStateButton = Button(ADD_TO_BELIEF_STATE_BUTTON_TEXT)

    /**
     * button that when clicked, indicates that the user wants to add the text
     * from [sentenceTextField] to the [Set] of sentences used for belief
     * revision.
     */
    val addForBeliefRevisionButton = Button(ADD_FOR_BELIEF_REVISION_BUTTON_TEXT)

    val initialBeliefStateDisplay = BeliefStateOutputPanel(INITIAL_BELIEF_STATE_LABEL_TEXT)

    val revisionSentencesDisplay = BeliefStateOutputPanel(REVISION_SENTENCES_LABEL_TEXT)

    val resultingBeliefStateDisplay = BeliefStateOutputPanel(RESULTING_BELIEF_STATE_LABEL_TEXT)

    val revisionConfigurationPanel = RevisionConfigurationPanel()

    val performRevisionButton = Button(PERFORM_REVISION_BUTTON_TEXT)

    val commitRevisionButton = Button(COMMIT_REVISION_BUTTON_TEXT)

    override fun start(primaryStage:Stage)
    {
        // configure the stage (the window)
        primaryStage.title = "Belief Reviser"

        // configure the scene (inside the window)
        val borderPane = BorderPane()
        primaryStage.scene = Scene(borderPane,800.0,500.0)
        primaryStage.scene.stylesheets.add(CSS.FILE_PATH)

        borderPane.top = VBox()
            .apply()
            {
                val buttonFlowPane = HBox(addToBeliefStateButton,addForBeliefRevisionButton)
                buttonFlowPane.spacing = Dimens.KEYLINE_SMALL.toDouble()

                val spacer = Region()
                spacer.prefWidth = Dimens.KEYLINE_SMALL.toDouble()
                spacer.prefHeight = Dimens.KEYLINE_SMALL.toDouble()

                padding = Insets(Dimens.KEYLINE_SMALL.toDouble())
                children.addAll(sentenceTextField,spacer,buttonFlowPane)
            }

        borderPane.center = HBox()
            .apply()
            {
                spacing = Dimens.KEYLINE_SMALL.toDouble()
                padding = Insets(Dimens.KEYLINE_SMALL.toDouble())
                children.addAll(initialBeliefStateDisplay,revisionSentencesDisplay,resultingBeliefStateDisplay)
            }

        borderPane.bottom = HBox()
            .apply()
            {
                val buttonPanel = HBox(performRevisionButton,commitRevisionButton)
                buttonPanel.spacing = Dimens.KEYLINE_SMALL.toDouble()
                buttonPanel.alignment = Pos.BOTTOM_RIGHT

                HBox.setHgrow(revisionConfigurationPanel,Priority.ALWAYS)

                spacing = Dimens.KEYLINE_SMALL.toDouble()
                padding = Insets(Dimens.KEYLINE_SMALL.toDouble())
                children.addAll(revisionConfigurationPanel,buttonPanel)
            }

        // display the window
        primaryStage.show()
    }
}
