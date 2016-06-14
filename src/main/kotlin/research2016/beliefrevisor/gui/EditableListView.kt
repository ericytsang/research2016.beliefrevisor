package research2016.beliefrevisor.gui

import javafx.beans.InvalidationListener
import javafx.event.EventHandler
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.TextInputDialog
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

abstract class EditableListView<Model>(private val entryName:String):VBox()
{
    protected abstract fun parse(string:String):Model
    protected abstract fun toInputString(entry:Model):String
    protected abstract fun addToList(existingEntries:MutableList<Model>,indexOfEntry:Int,newEntry:Model):String?
    protected abstract fun removeFromList(existingEntries:MutableList<Model>,indexOfEntry:Int):String?
    protected abstract fun updateListAt(existingEntries:MutableList<Model>,indexOfEntry:Int,newEntry:Model):String?

    val listView = ListView<Model>().apply()
    {
        // allow editing of sentences by double-clicking on them
        onMouseClicked = EventHandler()
        {
            event ->

            // continue if it is a double click, and an item in the list is selected
            if (!(event.clickCount == 2 && focusModel.focusedItem != null))
            {
                return@EventHandler
            }

            var previousResultString:String? = null
            while (true)
            {
                // show text input dialog to get sentence from user
                val result = TextInputDialog(previousResultString ?: toInputString(focusModel.focusedItem!!))
                    .apply()
                    {
                        title = "Edit Existing ${entryName.capitalize()}"
                        headerText = "Please enter the sentence below."
                    }
                    .showAndWait()

                // break if input is cancelled
                if (!result.isPresent)
                {
                    break
                }

                // otherwise, save the input don't have to retype what was already
                // entered if there is a problem later...
                else
                {
                    previousResultString = result.get()
                }

                // try to parse input
                val entry = try
                {
                    parse(result.get())
                }

                // there was an exception while parsing the result...show error
                // then try to get the input again
                catch (ex:Exception)
                {
                    // input format is invalid
                    val alert = Alert(Alert.AlertType.ERROR)
                    alert.title = "Edit Existing ${entryName.capitalize()}"
                    alert.headerText = "Invalid format"
                    alert.contentText = ex.message
                    alert.showAndWait()

                    // try to get input from user again again
                    continue
                }

                // try to add the entry to the list
                val errorMessage = updateListAt(items,focusModel.focusedIndex,entry)
                if (errorMessage == null)
                {
                    break
                }
                else
                {
                    // constraints not satisfied
                    val alert = Alert(Alert.AlertType.ERROR)
                    alert.title = "Edit Existing ${entryName.capitalize()}"
                    alert.headerText = "Unable to update $entryName"
                    alert.contentText = errorMessage
                    alert.showAndWait()

                    // try to get input from user again again
                    continue
                }
            }
        }

        onKeyPressed = EventHandler()
        {
            event ->
            when (event.code)
            {
                // allow deletion of entries by pressing delete or backspace
                in setOf(KeyCode.DELETE,KeyCode.BACK_SPACE) ->
                {
                    // try to remove the entry from the list
                    removeFromList(items,focusModel.focusedIndex)?.let()
                    {
                        errorMessage ->
                        val alert = Alert(Alert.AlertType.ERROR)
                        alert.title = "Remove Existing ${entryName.capitalize()}"
                        alert.headerText = "Unable to remove $entryName"
                        alert.contentText = errorMessage
                        alert.showAndWait()
                    }
                }

                // allow addition of entries by pressing enter
                KeyCode.ENTER ->
                {
                    addNewEntry(focusModel.focusedIndex+1)
                }

                // allow insertion of entries by pressing insert
                KeyCode.INSERT ->
                {
                    if (focusModel.focusedIndex in items.indices)
                    {
                        addNewEntry(focusModel.focusedIndex)
                    }
                    else
                    {
                        addNewEntry(focusModel.focusedIndex+1)
                    }
                }

                else -> {/* do nothing */}
            }
        }
    }

    val moveUpButton = Button("Move up").apply()
    {
        // only enable the button when the focused item is valid
        listView.focusModel.focusedIndexProperty().addListener(InvalidationListener()
        {
            isDisable = listView.focusModel.focusedIndex !in listView.items.indices
                || listView.focusModel.focusedIndex == listView.items.indices.first
        }.apply {invalidated(null)})

        // move the focused item up one position when clicked
        onAction = EventHandler()
        {
            val itemToMove = listView.focusModel.focusedItem
            val positionToMoveTo = listView.focusModel.focusedIndex-1
            val positionToRemoveFrom = listView.focusModel.focusedIndex
            listView.items.removeAt(positionToRemoveFrom)
            listView.items.add(positionToMoveTo,itemToMove)
            listView.focusModel.focus(positionToMoveTo)
        }
    }

    val moveDownButton = Button("Move down").apply()
    {
        // only enable the button when the focused item is valid
        listView.focusModel.focusedIndexProperty().addListener(InvalidationListener()
        {
            isDisable = listView.focusModel.focusedIndex !in listView.items.indices
                || listView.focusModel.focusedIndex == listView.items.indices.last
        }.apply {invalidated(null)})

        // move the focused item down one position when clicked
        onAction = EventHandler()
        {
            val itemToMove = listView.focusModel.focusedItem
            val positionToMoveTo = listView.focusModel.focusedIndex+1
            val positionToRemoveFrom = listView.focusModel.focusedIndex
            listView.items.removeAt(positionToRemoveFrom)
            listView.items.add(positionToMoveTo,itemToMove)
            listView.focusModel.focus(positionToMoveTo)
        }
    }

    private fun addNewEntry(index:Int)
    {
        var previousResultString:String? = null
        while (true)
        {
            // show text input dialog to get input from user
            val result = TextInputDialog(previousResultString ?: "")
                .apply()
                {
                    title = "Add New ${entryName.capitalize()}"
                    headerText = "Please enter the $entryName below."
                }
                .showAndWait()

            // break if input is cancelled
            if (!result.isPresent)
            {
                break
            }

            // otherwise, save the input don't have to retype what was already
            // entered if there is a problem later...
            else
            {
                previousResultString = result.get()
            }

            // try to parse input
            val entry = try
            {
                parse(result.get())
            }

            // there was an exception while parsing the result...show error
            // then try to get the input again
            catch (ex:Exception)
            {
                // input format is invalid
                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = "Add New ${entryName.capitalize()}"
                alert.headerText = "Invalid format"
                alert.contentText = ex.message
                alert.showAndWait()

                // try to get input from user again again
                continue
            }

            // try to add the entry to the list
            val errorMessage = addToList(listView.items,index,entry)
            if (errorMessage == null)
            {
                break
            }
            else
            {
                // constraints not satisfied
                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = "Add New ${entryName.capitalize()}"
                alert.headerText = "Constraint error"
                alert.contentText = errorMessage
                alert.showAndWait()

                // try to get input from user again again
                continue
            }
        }
    }

    init
    {
        val buttons = HBox().apply()
        {
            spacing = Dimens.KEYLINE_SMALL.toDouble()
            children.addAll(moveUpButton,moveDownButton)
        }

        spacing = Dimens.KEYLINE_SMALL.toDouble()
        children.addAll(listView,buttons)
    }
}
