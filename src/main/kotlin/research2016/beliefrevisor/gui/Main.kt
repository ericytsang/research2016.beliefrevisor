package research2016.beliefrevisor.gui

import com.sun.javafx.collections.ObservableListWrapper
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
import research2016.propositionallogic.Proposition
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

fun main(args:Array<String>)
{
    Application.launch(Gui::class.java)
}

class Gui():Application()
{
    companion object
    {
        // GUI strings
        const val PERFORM_REVISION_BUTTON_TEXT = "Revise"
        const val COMMIT_REVISION_BUTTON_TEXT = "Commit"
        const val FILE_MENU_TEXT = "File"
        const val SAVE_MENU_ITEM_TEXT = "Save"
        const val LOAD_MENU_ITEM_TEXT = "Load"

        // keys of map that we serialize to save to files
        const val SAVE_MAP_INITIAL_BELIEF_STATE = "SAVE_MAP_INITIAL_BELIEF_STATE"
        const val SAVE_MAP_SENTENCES_FOR_REVISION = "SAVE_MAP_SENTENCES_FOR_REVISION"
        const val SAVE_MAP_RESULTING_BELIEF_STATE = "SAVE_MAP_RESULTING_BELIEF_STATE"
        const val SAVE_MAP_BELIEF_REVISION_CONFIGURATION = "SAVE_MAP_BELIEF_REVISION_CONFIGURATION"
        const val SAVE_MAP_TRUST_PARTITION_CONFIGURATION = "SAVE_MAP_TRUST_PARTITION_CONFIGURATION"
    }

    lateinit var primaryStage:Stage

    val initialBeliefStateDisplay = RevisionInputBeliefStatePanel()

    val revisionSentencesDisplay = RevisionInputSentencePanel()

    val resultingBeliefStateDisplay = RevisionOutputResultPanel()

    val revisionConfigurationPanel = RevisionFunctionConfigPanel()

    val partitionConfigurationPanel = TrustPartitionConfigPanel()

    val performRevisionButton = Button(PERFORM_REVISION_BUTTON_TEXT).apply()
    {
        // disable this button if there are no formulas specified for the
        // initial belief state, or sentences for revision
        val listener = InvalidationListener()
        {
            isDisable = !(initialBeliefStateDisplay.listView.listView.focusModel.focusedItem != null
                && revisionSentencesDisplay.listView.listView.focusModel.focusedItem != null
                && revisionConfigurationPanel.beliefRevisionStrategy != null
                && partitionConfigurationPanel.sentenceRevisionStrategy != null)
        }.apply {invalidated(null)}
        initialBeliefStateDisplay.listView.listView.focusModel.focusedItemProperty().addListener(listener)
        revisionSentencesDisplay.listView.listView.focusModel.focusedItemProperty().addListener(listener)
        revisionConfigurationPanel.listener = listener
        partitionConfigurationPanel.listener = listener

        // when the revision button is clicked, perform a belief revision
        setOnAction()
        {
            val initialBeliefState = initialBeliefStateDisplay.listView.listView.focusModel.focusedItem.propositions.toList()
            val sentenceRevisionStrategy = partitionConfigurationPanel.sentenceRevisionStrategy!!
            val sentence = revisionSentencesDisplay.listView.listView.focusModel.focusedItem.let {sentenceRevisionStrategy.revise(it.proposition)}
            val resultingBeliefState = revisionConfigurationPanel.beliefRevisionStrategy!!.revise(initialBeliefState.toSet(),sentence)
            resultingBeliefStateDisplay.listView.items.clear()
            resultingBeliefStateDisplay.listView.items.addAll(resultingBeliefState.toList().let {resultingBeliefStateDisplay.DisplayDecorator(it)})
        }
    }

    val commitRevisionButton = Button(COMMIT_REVISION_BUTTON_TEXT).apply()
    {
        resultingBeliefStateDisplay.listView.items.addListener(InvalidationListener()
        {
            o ->
            isDisable = resultingBeliefStateDisplay.listView.items.isEmpty()
        }.apply()
        {
            invalidated(null)
        })

        setOnAction()
        {
            val resultingBeliefState = resultingBeliefStateDisplay.listView.items.first()
                .let {it.propositions}.toSet()
                .let {initialBeliefStateDisplay.DisplayDecorator(it)}
            initialBeliefStateDisplay.listView.listView.items.add(resultingBeliefState)
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

    val topPanel = HBox().apply()
    {
        spacing = Dimens.KEYLINE_SMALL.toDouble()
        padding = Insets(Dimens.KEYLINE_SMALL.toDouble())
        children.addAll(initialBeliefStateDisplay,revisionSentencesDisplay,resultingBeliefStateDisplay)
        children.forEach {HBox.setHgrow(it,Priority.ALWAYS)}
    }

    val middlePanel = HBox().apply()
    {
        children.addAll(revisionConfigurationPanel,partitionConfigurationPanel)
        spacing = Dimens.KEYLINE_SMALL.toDouble()
        padding = Insets(Dimens.KEYLINE_SMALL.toDouble())

        HBox.setHgrow(revisionConfigurationPanel,Priority.ALWAYS)
        HBox.setHgrow(partitionConfigurationPanel,Priority.ALWAYS)
    }


    val bottomPanel = HBox().apply()
    {
        children.addAll(performRevisionButton,commitRevisionButton)
        spacing = Dimens.KEYLINE_SMALL.toDouble()
        padding = Insets(Dimens.KEYLINE_SMALL.toDouble())
        alignment = Pos.BOTTOM_RIGHT
    }

    fun saveToFile()
    {
        val fileChooser = FileChooser()
        fileChooser.title = "Save State"
        val file = fileChooser.showSaveDialog(primaryStage)
        if (file != null)
        {
            val saveMap = mutableMapOf<String,Any>()
            saveMap.put(SAVE_MAP_INITIAL_BELIEF_STATE,initialBeliefStateDisplay.listView.listView.items.map {it.propositions} as Serializable)
            saveMap.put(SAVE_MAP_SENTENCES_FOR_REVISION,revisionSentencesDisplay.listView.listView.items.map {it.proposition} as Serializable)
            saveMap.put(SAVE_MAP_RESULTING_BELIEF_STATE,resultingBeliefStateDisplay.listView.items.map {it.propositions} as Serializable)
            saveMap.put(SAVE_MAP_BELIEF_REVISION_CONFIGURATION,revisionConfigurationPanel.saveToMap() as Serializable)
            saveMap.put(SAVE_MAP_TRUST_PARTITION_CONFIGURATION,partitionConfigurationPanel.saveToMap() as Serializable)
            ObjectOutputStream(file.outputStream()).apply()
            {
                writeObject(saveMap)
                close()
            }
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
            val objIns = ObjectInputStream(file.inputStream())
            val saveMap = objIns.readObject() as Map<String,Any>
            objIns.close()
            initialBeliefStateDisplay.listView.listView.items.clear()
            initialBeliefStateDisplay.listView.listView.items.addAll((saveMap[SAVE_MAP_INITIAL_BELIEF_STATE] as List<Set<Proposition>>).map {initialBeliefStateDisplay.DisplayDecorator(it)}.let {ObservableListWrapper(it)})
            revisionSentencesDisplay.listView.listView.items.clear()
            revisionSentencesDisplay.listView.listView.items.addAll((saveMap[SAVE_MAP_SENTENCES_FOR_REVISION] as List<Proposition>).map {revisionSentencesDisplay.DisplayDecorator(it)}.let {ObservableListWrapper(it)})
            resultingBeliefStateDisplay.listView.items.clear()
            resultingBeliefStateDisplay.listView.items.addAll((saveMap[SAVE_MAP_RESULTING_BELIEF_STATE] as List<List<Proposition>>).map {resultingBeliefStateDisplay.DisplayDecorator(it)})
            revisionConfigurationPanel.loadFromMap(saveMap[SAVE_MAP_BELIEF_REVISION_CONFIGURATION] as Map<String,Any>)
            partitionConfigurationPanel.loadFromMap(saveMap[SAVE_MAP_TRUST_PARTITION_CONFIGURATION] as Map<String,Any>)
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
            VBox.setVgrow(topPanel,Priority.ALWAYS)
            children.addAll(menuBar,topPanel,middlePanel,bottomPanel)
        }

        // configure the scene (inside the window)
        primaryStage.scene = Scene(vBox,800.0,500.0)
        primaryStage.scene.stylesheets.add(CSS.FILE_PATH)

        // display the window
        primaryStage.show()
    }
}
