import ComponentStyles.actionLink
import ComponentStyles.actionsField
import ComponentStyles.dateField
import ComponentStyles.nameField
import ComponentStyles.queueItem
import ComponentStyles.queueList
import react.*
import react.dom.*
import kotlinx.html.js.*
import kotlinx.coroutines.*
import kotlinx.css.*
import kotlinx.css.properties.TextDecoration
import kotlinx.css.properties.TextDecorationLine
import kotlinx.css.properties.TextDecorationStyle
import kotlinx.css.properties.border
import kotlinx.html.onClick
import styled.*
import kotlin.js.Date

private val scope = MainScope()

object ComponentStyles : StyleSheet("ComponentStyles", isStatic = true) {
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

    val dateField by css {
        +queueField
//        paddingRight = 0.px
        width = 150.px
//        border(1.px, BorderStyle.solid, Color.black)

    }

    val nameField by css {
        +queueField
//        padding(horizontal = 0.px)
        width = 400.px
        fontWeight = FontWeight.bold
        textDecoration = TextDecoration(setOf(TextDecorationLine.underline), TextDecorationStyle.solid)
        textAlign = TextAlign.end
//        border(1.px, BorderStyle.solid, Color.black)
    }

    val actionsField by css {
//        alignContent = Align.end
        marginTop = 15.px
        textAlign = TextAlign.end
    }

    val actionLink by css {
//        padding(horizontal = 0.px)
//        width = 150.px
//        border(1.px, BorderStyle.solid, Color.black)
        margin(horizontal = 15.px)
        visited { color=Color.blue }
    }
}


val App = functionalComponent<RProps> { _ ->
    println("App...")
    var jobList: List<SpleeterJob> by useState(emptyList())

    suspend fun refreshJobs() {
        jobList = Api.getJobList()
    }

    fun deleteItem(item: SpleeterJob) {
        scope.launch {
            Api.deleteJobListItem(item)
            refreshJobs()
        }
    }


    useEffect(dependencies = listOf()) {
        scope.launch {
            refreshJobs()
        }
    }

    h1 {
        +"Queue"
    }

    child(Upload) {
        attrs { onUploadComplete = ::refreshJobs }
    }

    styledUl {
        css { +queueList }

        jobList.sortedByDescending(SpleeterJob::ts).forEach { item ->
            styledLi {
                css { +queueItem }

//                key = item.toString()
                div {
                    styledDiv {
                        css { +dateField }
                        val d = Date(item.ts)
                        +"${d.toLocaleDateString("en-IL")} ${d.toLocaleTimeString("en-IL")}"
                    }

                    styledDiv {
                        css { +nameField }
                        +item.origFileName
                    }
                }

                styledDiv {
                    css { +actionsField }

                    if(item.status == JobStatus.Success || true) {
                        styledA(href = item.resultUri) {
                            css { +actionLink }
                            +"download"
                        }
                    }

                    styledA(href = "#") {
                        css { +actionLink }
                        attrs {
//                            text("Delete")
                            onClickFunction = { deleteItem(item) }
                        }

                        +"delete"
                    }
                }

//                attrs.onClickFunction = {
//                    scope.launch {
//                        Api.deleteJobListItem(item)
//                        refreshJobs()
//                    }
//                }
            }
        }
    }
}
