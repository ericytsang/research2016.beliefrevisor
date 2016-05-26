package research2016.beliefrevisor.gui

import javafx.scene.control.TextArea
import javafx.scene.layout.StackPane
import research2016.propositionallogic.Proposition

/**
 * Created by surpl on 5/25/2016.
 */
class BeliefStateOutputPanel:StackPane()
{
    private val textArea = TextArea()

    init
    {
        children.add(textArea)
    }

    var propositions:Set<Proposition> = emptySet()

        // update text area text every time this value is set
        set(value)
        {
            field = value
            textArea.text = propositions
                .fold(StringBuilder())
                {
                    stb,it -> stb.append("$it\n")
                }
                .toString()
        }
}
