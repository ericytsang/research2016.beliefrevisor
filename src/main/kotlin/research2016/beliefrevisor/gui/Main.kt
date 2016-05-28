package research2016.beliefrevisor.gui

import javafx.application.Application
import javafx.beans.InvalidationListener
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Stage
import research2016.beliefrevisor.core.TotalPreOrderBeliefRevisionStrategy
import research2016.propositionallogic.Proposition
import research2016.propositionallogic.Situation
import research2016.propositionallogic.and
import java.util.Comparator

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

    val initialBeliefStateDisplay = BeliefStateOutputPanel(INITIAL_BELIEF_STATE_LABEL_TEXT)

    val revisionSentencesDisplay = BeliefStateOutputPanel(REVISION_SENTENCES_LABEL_TEXT)

    val resultingBeliefStateDisplay = BeliefStateOutputPanel(RESULTING_BELIEF_STATE_LABEL_TEXT)

    val sentenceTextField = InputPane()
        .apply()
        {
            val listener = object:BeliefStateOutputPanel.Listener()
            {
                override fun onPropositionDoubleClicked(proposition:Proposition)
                {
                    sentenceTextField.text = proposition.toString()
                }
            }
            initialBeliefStateDisplay.listeners.add(listener)
            revisionSentencesDisplay.listeners.add(listener)
            resultingBeliefStateDisplay.listeners.add(listener)
        }

    /**
     * button that when clicked, indicates that the user wants to add the text
     * from [sentenceTextField] to belief state.
     */
    val addToBeliefStateButton = Button(ADD_TO_BELIEF_STATE_BUTTON_TEXT)
        .apply()
        {
            sentenceTextField.sentenceTextField.textProperty().addListener(InvalidationListener()
            {
                isDisable = sentenceTextField.sentenceTextField.sentence == null
            }.apply {invalidated(null)})

            setOnAction()
            {
                sentenceTextField.sentenceTextField.sentence?.let()
                {
                    initialBeliefStateDisplay.propositions += it
                }
            }
        }

    /**
     * button that when clicked, indicates that the user wants to add the text
     * from [sentenceTextField] to the [Set] of sentences used for belief
     * revision.
     */
    val addForBeliefRevisionButton = Button(ADD_FOR_BELIEF_REVISION_BUTTON_TEXT)
        .apply()
        {
            sentenceTextField.sentenceTextField.textProperty().addListener(InvalidationListener()
            {
                isDisable = sentenceTextField.sentenceTextField.sentence == null
            }.apply {invalidated(null)})

            setOnAction()
            {
                sentenceTextField.sentenceTextField.sentence?.let()
                {
                    revisionSentencesDisplay.propositions += it
                }
            }
        }

    val revisionConfigurationPanel = RevisionConfigurationPanel()

    val performRevisionButton = Button(PERFORM_REVISION_BUTTON_TEXT)
        .apply()
        {
            // disable this button if there are no formulas specified for the
            // initial belief state, or sentences for revision
            val listener = object:BeliefStateOutputPanel.Listener()
            {
                override fun onItemsChanged()
                {
                    isDisable = initialBeliefStateDisplay.propositions.isEmpty() ||
                        revisionSentencesDisplay.propositions.isEmpty()
                }
            }.apply {onItemsChanged()}
            initialBeliefStateDisplay.listeners.add(listener)
            revisionSentencesDisplay.listeners.add(listener)

            // when the revision button is clicked, perform a belief revision
            setOnAction()
            {
                val situationComparatorFactory = fun(initialBeliefState:Set<Proposition>):Comparator<Situation>
                {
                    return revisionConfigurationPanel.situationComparator(initialBeliefState)
                }
                val initialBeliefState = initialBeliefStateDisplay.propositions.toSet()
                val sentence = revisionSentencesDisplay.propositions.fold<Proposition,Proposition?>(null) {i,n -> i?.let {i and n} ?: n}!!
                val resultingBeliefState = TotalPreOrderBeliefRevisionStrategy(situationComparatorFactory).revise(initialBeliefState,sentence)
                resultingBeliefStateDisplay.propositions = resultingBeliefState.toList()
            }
        }

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
                val buttonPanel = HBox(addToBeliefStateButton,addForBeliefRevisionButton)
                buttonPanel.spacing = Dimens.KEYLINE_SMALL.toDouble()

                spacing = Dimens.KEYLINE_SMALL.toDouble()
                padding = Insets(Dimens.KEYLINE_SMALL.toDouble())
                children.addAll(sentenceTextField,buttonPanel)
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
