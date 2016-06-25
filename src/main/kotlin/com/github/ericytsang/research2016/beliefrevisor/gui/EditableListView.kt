package com.github.ericytsang.research2016.beliefrevisor.gui

import javafx.beans.InvalidationListener
import javafx.event.EventHandler
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.Dialog
import javafx.scene.control.ListView
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import java.util.Optional

abstract class EditableListView<Model,InputDialog:Dialog<ResultType>,ResultType>:VBox()
{
    protected abstract fun tryParseInput(inputDialog:InputDialog):Model
    protected abstract fun makeInputDialog(model:Model?):InputDialog
    protected abstract fun isInputCancelled(result:Optional<ResultType>):Boolean

    protected open fun tryAddToListAt(existingEntries:MutableList<Model>,indexOfEntry:Int,newEntry:Model)
    {
        existingEntries.add(indexOfEntry,newEntry)
    }

    protected open fun tryRemoveFromListAt(existingEntries:MutableList<Model>,indexOfEntry:Int)
    {
        existingEntries.removeAt(indexOfEntry)
    }

    protected open fun tryUpdateListAt(existingEntries:MutableList<Model>,indexOfEntry:Int,newEntry:Model)
    {
        existingEntries[indexOfEntry] = newEntry
    }

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

            val inputDialog = makeInputDialog(focusModel.focusedItem)
            while (true)
            {
                // show input dialog to get input from user
                val result = inputDialog
                    .apply {title = "Edit Existing Entry"}
                    .showAndWait()

                // break if input is cancelled
                if (isInputCancelled(result))
                {
                    break
                }

                // try to parse input
                val entry = try
                {
                    tryParseInput(inputDialog)
                }

                // there was an exception while parsing the result...show error
                // then try to get the input again
                catch (ex:Exception)
                {
                    // input format is invalid
                    val alert = Alert(Alert.AlertType.ERROR)
                    alert.title = "Edit Existing Entry"
                    alert.headerText = "Invalid input format"
                    alert.contentText = ex.message
                    alert.showAndWait()

                    // try to get input from user again again
                    continue
                }

                // try to add the entry to the list
                try
                {
                    tryUpdateListAt(items,focusModel.focusedIndex,entry)
                    break
                }
                catch (ex:Exception)
                {
                    // constraints not satisfied
                    val alert = Alert(Alert.AlertType.ERROR)
                    alert.title = "Edit Existing Entry"
                    alert.headerText = "Unable to update entry"
                    alert.contentText = ex.message
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
                    try
                    {
                        tryRemoveFromListAt(items,focusModel.focusedIndex)
                    }
                    catch (ex:Exception)
                    {
                        val alert = Alert(Alert.AlertType.ERROR)
                        alert.title = "Remove Existing Entry"
                        alert.headerText = "Unable to remove entry"
                        alert.contentText = ex.message
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

                else ->
                {/* do nothing */
                }
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

    val addButton = Button("Add").apply()
    {
        onAction = EventHandler()
        {
            if (listView.focusModel.focusedItem != null)
            {
                addNewEntry(listView.focusModel.focusedIndex+1)
            }
            else
            {
                addNewEntry(0)
            }
        }
    }

    private fun addNewEntry(index:Int)
    {
        val inputDialog = makeInputDialog(null)
        while (true)
        {
            // show text input dialog to get input from user
            val result = inputDialog
                .apply {title = "Add New Entry"}
                .showAndWait()

            // break if input is cancelled
            if (isInputCancelled(result))
            {
                break
            }

            // try to parse input
            val entry = try
            {
                tryParseInput(inputDialog)
            }

            // there was an exception while parsing the result...show error
            // then try to get the input again
            catch (ex:Exception)
            {
                // input format is invalid
                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = "Add New Entry"
                alert.headerText = "Invalid input format"
                alert.contentText = ex.message
                alert.showAndWait()

                // try to get input from user again again
                continue
            }

            // try to add the entry to the list
            try
            {
                tryAddToListAt(listView.items,index,entry)
                break
            }
            catch (ex:Exception)
            {
                // constraints not satisfied
                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = "Add New Entry"
                alert.headerText = "Constraint error"
                alert.contentText = ex.message
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
            children.addAll(moveUpButton,moveDownButton,addButton)
        }

        spacing = Dimens.KEYLINE_SMALL.toDouble()
        children.addAll(listView,buttons)
        setVgrow(listView,Priority.ALWAYS)
    }
}
