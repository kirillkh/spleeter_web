import io.ktor.http.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*

import kotlinx.browser.window
import org.khronos.webgl.Uint8Array
import org.w3c.xhr.FormData
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestUpload
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Date

object Api {
    val endpoint = window.location.origin // only needed until https://github.com/ktorio/ktor/issues/1695 is resolved

    val jsonClient = HttpClient {
        install(JsonFeature) { serializer = KotlinxSerializer() }
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
        println("getShoppingList()")
        return jsonClient.get(endpoint + SpleeterJob.path)
    }

    suspend fun deleteJobListItem(spleeterJob: SpleeterJob) {
        println("deleteShoppingListItem()")
        val (msb, lsb) =  spleeterJob.id.let { Pair(it.msb, it.lsb) }
        val encoded = uuidToIntArray(spleeterJob.id).joinToString(",")

        jsonClient.delete<Unit>(endpoint + SpleeterJob.path + "/$encoded")
    }
}