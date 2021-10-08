import Api.downloadUri
import QueueFieldStyles.actionsField
import QueueFieldStyles.dateField
import QueueFieldStyles.nameField
import QueueStyles.actionLink
import QueueStyles.queueItem
import QueueStyles.queueList
import StatusStyles.statusFieldFailure
import StatusStyles.statusFieldInProgress
import StatusStyles.statusFieldSuccess
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.html.js.*
import react.*
import react.dom.*
import styled.*
import kotlin.js.Date

private val scope = MainScope()

var timer: dynamic = null

val App = fc<PropsWithChildren> { _ ->
    println("App...")
    var jobList: List<SpleeterJob> by useState(emptyList())

    suspend fun refreshJobs() {
        println("refreshJobs() called")
        jobList = Api.getJobList()
    }

    fun deleteItem(item: SpleeterJob) {
        scope.launch {
            Api.deleteJobListItem(item)
            refreshJobs()
        }
    }


    useEffect {
        scope.launch {
            refreshJobs()
        }
    }

    useEffect {
        val handler = {
            scope.launch {
                refreshJobs()
            }
        }

        scope.launch {
            println("calling2 setTimeout()")
            if(timer != null)
                window.clearTimeout(timer)
            timer = window.setTimeout(handler, 1000)
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
                        css { +nameField }
                        +item.origFileName
                    }

                    styledDiv {
                        css {
                            + when(item.status) {
                                JobStatus.InProgress -> statusFieldInProgress
                                JobStatus.Success -> statusFieldSuccess
                                JobStatus.Failure -> statusFieldFailure
                            }
                        }
                        +item.status.toString()
                    }
                }

                div {
                    styledDiv {
                        css { +dateField }
                        val d = Date(item.ts)
                        +"${d.toLocaleDateString("en-IL")} ${d.toLocaleTimeString("en-IL")}"
                    }

                    styledDiv {
                        css { +actionsField }

                        if (item.status == JobStatus.Success) {
                            styledA(href = downloadUri(item)) {
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
