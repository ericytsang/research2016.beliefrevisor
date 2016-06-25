package com.github.ericytsang.research2016.beliefrevisor

import javafx.application.Application
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.TextField
import javafx.stage.Stage
import org.junit.Test

/**
 * Created by surpl on 6/13/2016.
 */
class AlertDialogTests:Application()
{
    override fun start(primaryStage:Stage?)
    {
        val node = TextField()

        val result = Alert(Alert.AlertType.CONFIRMATION)
            .apply()
            {
                dialogPane.content = node
            }.showAndWait()

        println("${result.get() == ButtonType.OK}, $result, ${node.text}")

        Alert(Alert.AlertType.INFORMATION)
            .apply()
            {
                dialogPane.content = node
                showAndWait()
                showAndWait()
                showAndWait()
                showAndWait()
                showAndWait()
                showAndWait()
            }
    }

    @Test
    fun canUseSameNodeOnDifferentInstancesOfAlertDialogsShownAtDifferentTimes()
    {
        launch(AlertDialogTests::class.java)
    }
}
