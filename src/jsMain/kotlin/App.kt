import react.*
import react.dom.*
import kotlinx.html.js.*
import kotlinx.coroutines.*

private val scope = MainScope()

val App = functionalComponent<RProps> { _ ->
    println("App...")
    val (jobList, setJobList) = useState(emptyList<SpleeterJob>())

    suspend fun refreshJobs() = setJobList(Api.getJobList())


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

    child(UploadComponent::class) {
        attrs {
            onUploadComplete = { refreshJobs() }
        }
    }
}
