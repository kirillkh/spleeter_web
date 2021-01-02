import QueueStyles.queueField
import QueueFieldStyles.statusField
import kotlinx.css.*
import kotlinx.css.properties.TextDecoration
import kotlinx.css.properties.TextDecorationLine
import kotlinx.css.properties.TextDecorationStyle
import styled.StyleSheet

object QueueStyles : StyleSheet("ComponentStyles", isStatic = true) {
    val queueList by css {
        display = Display.table
        borderBottom = "1px solid black"
        padding(0.px)
    }

    val queueItem by css {
        borderTop = "1px solid black"
        borderLeft = "1px solid black"
        borderRight = "1px solid black"
        listStyleType = ListStyleType.none
        padding(5.px)
    }

    val queueField by css {
        display = Display.inlineBlock
        padding(horizontal = 0.px)
//        border(1.px, BorderStyle.solid, Color.black)
    }

    val actionLink by css {
//        padding(horizontal = 0.px)
//        width = 150.px
//        border(1.px, BorderStyle.solid, Color.black)
        margin(horizontal = 15.px)
        visited { color= Color.blue }
    }
}


object QueueFieldStyles : StyleSheet("ComponentStyles", isStatic = true) {
    val nameField by css {
        +queueField
//        padding(horizontal = 0.px)
        width = 400.px
        fontWeight = FontWeight.bold
        textDecoration = TextDecoration(setOf(TextDecorationLine.underline), TextDecorationStyle.solid)
        textAlign = TextAlign.start
//        border(1.px, BorderStyle.solid, Color.black)
    }

    val statusField by css {
        +queueField
//        padding(horizontal = 0.px)
        width = 150.px
        fontWeight = FontWeight.bold
        textAlign = TextAlign.end
//        border(1.px, BorderStyle.solid, Color.black)
    }


    val dateField by css {
        +queueField
//        paddingRight = 0.px
        marginTop = 15.px
        width = 400.px
//        border(1.px, BorderStyle.solid, Color.black)
        textAlign = TextAlign.start

        fontFamily = "monospace"
        put("font-size", "small")
    }

    val actionsField by css {
//        alignContent = Align.end
        +queueField
        width = 150.px
        marginTop = 15.px
        textAlign = TextAlign.end
    }
}


object StatusStyles : StyleSheet("ComponentStyles", isStatic = true) {
    val statusFieldInProgress by css {
        +statusField
    }

    val statusFieldSuccess by css {
        +statusField
        color = Color.green
    }

    val statusFieldFailure by css {
        +statusField
        color = Color.red
    }
}
