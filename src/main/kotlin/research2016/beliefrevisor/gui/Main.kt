package research2016.beliefrevisor.gui

import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import research2016.beliefrevisor.gui.CSS


fun main(args:Array<String>)
{
    Application.launch(Gui::class.java)
}

class Gui():Application()
{
    override fun start(primaryStage:Stage)
    {
        // configure the stage (the window)
        primaryStage.title = "Belief Reviser"

        // configure the scene (inside the window)
        val borderPane = BorderPane()
        primaryStage.scene = Scene(borderPane,800.0,500.0)
        primaryStage.scene.stylesheets.add(CSS.FILE_PATH)

        val inputPane = InputPane()
        borderPane.center = inputPane
        inputPane.padding = Insets(10.0)

        // display the window
        primaryStage.show()
    }
}
