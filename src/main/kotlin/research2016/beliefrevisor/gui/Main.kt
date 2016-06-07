package research2016.beliefrevisor.gui

import javafx.application.Application
import javafx.beans.InvalidationListener
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Stage
import research2016.propositionallogic.And
import research2016.propositionallogic.Contradiction
import research2016.propositionallogic.Proposition
import research2016.propositionallogic.toParsableString
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

fun main(args:Array<String>)
{
    Application.launch(Gui::class.java)
}

class Gui():Application()
{
    companion object
    {
        // GUI strings
        const val ADD_TO_BELIEF_STATE_BUTTON_TEXT = "Add to Initial Belief State"
        const val ADD_FOR_BELIEF_REVISION_BUTTON_TEXT = "Add for Belief Revision"
        const val PERFORM_REVISION_BUTTON_TEXT = "Revise"
        const val COMMIT_REVISION_BUTTON_TEXT = "Commit"
        const val INITIAL_BELIEF_STATE_LABEL_TEXT = "Initial Belief State"
        const val REVISION_SENTENCES_LABEL_TEXT = "Sentences for Revision"
        const val RESULTING_BELIEF_STATE_LABEL_TEXT = "Resulting Belief State"
        const val FILE_MENU_TEXT = "File"
        const val SAVE_MENU_ITEM_TEXT = "Save"
        const val LOAD_MENU_ITEM_TEXT = "Load"

        // keys of map that we serialize to save to files
        const val SAVE_MAP_INITIAL_BELIEF_STATE = "SAVE_MAP_INITIAL_BELIEF_STATE"
        const val SAVE_MAP_SENTENCES_FOR_REVISION = "SAVE_MAP_SENTENCES_FOR_REVISION"
        const val SAVE_MAP_RESULTING_BELIEF_STATE = "SAVE_MAP_RESULTING_BELIEF_STATE"
        const val SAVE_MAP_BELIEF_REVISION_CONFIGURATION = "SAVE_MAP_BELIEF_REVISION_CONFIGURATION"
    }

    lateinit var primaryStage:Stage

    val initialBeliefStateDisplay = BeliefStateOutputPanel(INITIAL_BELIEF_STATE_LABEL_TEXT)

    val revisionSentencesDisplay = BeliefStateOutputPanel(REVISION_SENTENCES_LABEL_TEXT)

    val resultingBeliefStateDisplay = BeliefStateOutputPanel(RESULTING_BELIEF_STATE_LABEL_TEXT)

    val sentenceTextField = InputPane().apply()
    {
        val listener = object:BeliefStateOutputPanel.Observer()
        {
            override fun onPropositionDoubleClicked(proposition:Proposition)
            {
                sentenceTextField.text = proposition.toParsableString()
            }
        }
        initialBeliefStateDisplay.observers.add(listener)
        revisionSentencesDisplay.observers.add(listener)
        resultingBeliefStateDisplay.observers.add(listener)
    }

    /**
     * button that when clicked, indicates that the user wants to add the text
     * from [sentenceTextField] to belief state.
     */
    val addToBeliefStateButton = Button(ADD_TO_BELIEF_STATE_BUTTON_TEXT).apply()
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
    val addForBeliefRevisionButton = Button(ADD_FOR_BELIEF_REVISION_BUTTON_TEXT).apply()
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

    val performRevisionButton = Button(PERFORM_REVISION_BUTTON_TEXT).apply()
    {
        // disable this button if there are no formulas specified for the
        // initial belief state, or sentences for revision
        val listener = object:BeliefStateOutputPanel.Observer()
        {
            override fun onItemsChanged()
            {
                isDisable = initialBeliefStateDisplay.propositions.isEmpty() ||
                    revisionSentencesDisplay.propositions.isEmpty()
            }
        }.apply {onItemsChanged()}
        initialBeliefStateDisplay.observers.add(listener)
        revisionSentencesDisplay.observers.add(listener)

        // when the revision button is clicked, perform a belief revision
        setOnAction()
        {
            val initialBeliefState = initialBeliefStateDisplay.propositions.toSet()
            val sentence = revisionSentencesDisplay.propositions.toList().let {if (it.isEmpty()) Contradiction else And.make(it)}
            val resultingBeliefState = revisionConfigurationPanel.beliefRevisionStrategy.revise(initialBeliefState,sentence)
            resultingBeliefStateDisplay.propositions = resultingBeliefState.toList()
        }
    }

    val commitRevisionButton = Button(COMMIT_REVISION_BUTTON_TEXT).apply()
    {
        resultingBeliefStateDisplay.observers.add(object:BeliefStateOutputPanel.Observer()
        {
            override fun onItemsChanged()
            {
                isDisable = resultingBeliefStateDisplay.propositions.isEmpty()
            }
        }.apply {onItemsChanged()})

        setOnAction()
        {
            initialBeliefStateDisplay.propositions = resultingBeliefStateDisplay.propositions
        }
    }

    val menuBar = MenuBar().apply()
    {
        val fileMenu = Menu(FILE_MENU_TEXT).apply()
        {
            val saveMenuItem = MenuItem(SAVE_MENU_ITEM_TEXT).apply()
            {
                onAction = EventHandler {saveToFile()}
            }
            val loadMenuItem = MenuItem(LOAD_MENU_ITEM_TEXT).apply()
            {
                onAction = EventHandler {loadFromFile()}
            }
            items.addAll(saveMenuItem,loadMenuItem)
        }
        menus.addAll(fileMenu)
    }

    val topPanel = VBox().apply()
    {
        val buttonPanel = HBox(addToBeliefStateButton,addForBeliefRevisionButton)
        buttonPanel.spacing = Dimens.KEYLINE_SMALL.toDouble()

        spacing = Dimens.KEYLINE_SMALL.toDouble()
        padding = Insets(Dimens.KEYLINE_SMALL.toDouble())
        children.addAll(sentenceTextField,buttonPanel)
    }

    val middlePanel = HBox().apply()
    {
        spacing = Dimens.KEYLINE_SMALL.toDouble()
        padding = Insets(Dimens.KEYLINE_SMALL.toDouble())
        children.addAll(initialBeliefStateDisplay,revisionSentencesDisplay,resultingBeliefStateDisplay)
        children.forEach {HBox.setHgrow(it,Priority.ALWAYS)}
    }

    val bottomPanel = HBox().apply()
    {
        val buttonPanel = HBox(performRevisionButton,commitRevisionButton)
        buttonPanel.spacing = Dimens.KEYLINE_SMALL.toDouble()
        buttonPanel.alignment = Pos.BOTTOM_RIGHT

        HBox.setHgrow(revisionConfigurationPanel,Priority.ALWAYS)

        spacing = Dimens.KEYLINE_SMALL.toDouble()
        padding = Insets(Dimens.KEYLINE_SMALL.toDouble())
        children.addAll(revisionConfigurationPanel,buttonPanel)
    }

    fun saveToFile()
    {
        val fileChooser = FileChooser()
        fileChooser.title = "Save State"
        val file = fileChooser.showSaveDialog(primaryStage)
        if (file != null)
        {
            val saveMap = mutableMapOf<String,Any>()
            saveMap.put(SAVE_MAP_INITIAL_BELIEF_STATE,initialBeliefStateDisplay.propositions)
            saveMap.put(SAVE_MAP_SENTENCES_FOR_REVISION,revisionSentencesDisplay.propositions)
            saveMap.put(SAVE_MAP_RESULTING_BELIEF_STATE,resultingBeliefStateDisplay.propositions)
            saveMap.put(SAVE_MAP_BELIEF_REVISION_CONFIGURATION,revisionConfigurationPanel.saveToMap())
            ObjectOutputStream(file.outputStream()).writeObject(saveMap)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun loadFromFile()
    {
        val fileChooser = FileChooser()
        fileChooser.title = "Load State"
        val file = fileChooser.showOpenDialog(primaryStage)
        if (file != null)
        {
            val saveMap = ObjectInputStream(file.inputStream()).readObject() as Map<String,Any>
            initialBeliefStateDisplay.propositions = saveMap[SAVE_MAP_INITIAL_BELIEF_STATE] as List<Proposition>
            revisionSentencesDisplay.propositions = saveMap[SAVE_MAP_SENTENCES_FOR_REVISION] as List<Proposition>
            resultingBeliefStateDisplay.propositions = saveMap[SAVE_MAP_RESULTING_BELIEF_STATE] as List<Proposition>
            revisionConfigurationPanel.loadFromMap(saveMap[SAVE_MAP_BELIEF_REVISION_CONFIGURATION] as Map<String,Any>)
        }
    }

    override fun start(primaryStage:Stage)
    {
        // configure the stage (the window)
        this.primaryStage = primaryStage
        primaryStage.title = "Belief Reviser"

        // create layouts to be added to the scene
        val vBox = VBox().apply()
        {
            VBox.setVgrow(middlePanel,Priority.ALWAYS)
            children.addAll(menuBar,topPanel,middlePanel,bottomPanel)
        }

        // configure the scene (inside the window)
        primaryStage.scene = Scene(vBox,800.0,500.0)
        primaryStage.scene.stylesheets.add(CSS.FILE_PATH)

        // display the window
        primaryStage.show()
    }
}
