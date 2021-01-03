import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.browser.window
import org.w3c.xhr.FormData
import org.w3c.xhr.XMLHttpRequest
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Date

object Api {
    val endpoint = window.location.origin // only needed until https://github.com/ktorio/ktor/issues/1695 is resolved

    val jsonClient = HttpClient {
        install(JsonFeature) { serializer = KotlinxSerializer() }
    }

    fun uuidToParam(spleeterJob: SpleeterJob) =
        uuidToIntArray(spleeterJob.id).joinToString(",")

    fun downloadUri(item: SpleeterJob): String {
        val dlName = item.origFileName.substringBeforeLast('.') + "-minus.mp3"
        return endpoint + SpleeterJob.path + "/d/${uuidToParam(item)}/$dlName"
    }


    suspend fun uploadData(data: FormData): String {
        val xhr = XMLHttpRequest()
        val since = Date().toISOString().subSequence(0, 10)
        xhr.open("post", endpoint + SpleeterJob.path + "?since=" + since)
        xhr.send(data)

        return suspendCoroutine { cont ->
            xhr.onloadend = {
                cont.resume(it.type)
            }
        }
    }

    suspend fun getJobList(): List<SpleeterJob> {
        println("getJobList()")
        return jsonClient.get(endpoint + SpleeterJob.path)
    }

    suspend fun deleteJobListItem(spleeterJob: SpleeterJob) {
        println("deleteJobListItem()")
        val encoded = uuidToParam(spleeterJob)
        jsonClient.delete<Unit>(endpoint + SpleeterJob.path + "/$encoded")
    }
}