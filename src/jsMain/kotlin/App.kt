import react.*
import react.dom.*
import kotlinx.html.js.*
import kotlinx.coroutines.*

private val scope = MainScope()

val App = functionalComponent<RProps> { _ ->
    println("App...")
    var jobList: List<SpleeterJob> by useState(emptyList())

    suspend fun refreshJobs() {
        jobList = Api.getJobList()
    }


    useEffect(dependencies = listOf()) {
        scope.launch {
            refreshJobs()
        }
    }

    h1 {
        +"Queue"
    }
    ul {
        jobList.sortedByDescending(SpleeterJob::ts).forEach { item ->
            li {
                key = item.toString()
                +"[${item.ts}] ${item.origFileName} "

                attrs.onClickFunction = {
                    scope.launch {
                        Api.deleteJobListItem(item)
                        refreshJobs()
                    }
                }
            }
        }
    }

    child(Upload) {
        attrs {
            onUploadComplete = { refreshJobs() }
        }
    }
}
