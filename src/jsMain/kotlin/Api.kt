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


//    @UseExperimental(ImplicitReflectionSerializer::class)
//    fun postMultipartEditProfile(binaryString: String, successBlock: (Profile) -> Unit) {
//
//
//        val address = Url("$BASE_URL/$PROFILES/$EDIT").toString()
//        println("address = $address")
//        GlobalScope.apply {
//            launch(ApplicationDispatcher) {
//                val result: Profile = networkHttpClient.post {
//                    url(address)
//                    body = MultiPartContent.build {
//                        add("file", binaryString.toByteArray(), filename = "binary.bin")
//                    }
//                }
//
//                println(result)
//                successBlock(result)
//            }
//        }
//    }






    suspend fun uploadData(
//        uploadFiles: Map<String, File>,
        fileName: String,
        bytes: ByteArray
    ): HttpResponse {
        return jsonClient.post<HttpResponse> {
//            url {
//                protocol = URLProtocol.HTTP
////                host = "api.server.com"
//                host = "127.0.0.1:9090"
//                path(SpleeterJob.path)
//                parameters.append("since", "2020-07-17")
//            }
            val since = Date().toISOString().subSequence(0, 10)
            url(endpoint + SpleeterJob.path + "?since=" + since)

            headers {
                append("Authorization", "XXXX")
                append("Accept", ContentType.Application.Json)
            }

            body = MultiPartFormDataContent(
                formData {
                    this.appendInput(
                        key = "file",
                        headers = Headers.build {
                            append(
                                HttpHeaders.ContentDisposition,
                                "filename=$fileName"
                            )
                        },
                        size = bytes.size.toLong()
                    ) { buildPacket { writeFully(bytes) } }
//                    texts.entries.forEach {
//                        this.append(FormPart(it.key, it.value))
//                    }
                }
            )
        }
    }


    suspend fun uploadData2(
//        uploadFiles: Map<String, File>,
        fileName: String,
        bytes: Memory
    ): HttpResponse {
        return jsonClient.post<HttpResponse> {
//            url {
//                protocol = URLProtocol.HTTP
////                host = "api.server.com"
//                host = "127.0.0.1:9090"
//                path(SpleeterJob.path)
//                parameters.append("since", "2020-07-17")
//            }
            val since = Date().toISOString().subSequence(0, 10)
            url(endpoint + SpleeterJob.path + "?since=" + since)

            headers {
                append("Authorization", "XXXX")
                append("Accept", ContentType.Application.Json)
            }

            body = MultiPartFormDataContent(
                formData {
                    this.appendInput(
                        key = "file",
                        headers = Headers.build {
                            append(
                                HttpHeaders.ContentDisposition,
                                "filename=$fileName"
                            )
                        },
                        size = bytes.size
                    ) {


                        buildPacket {
//                            writeFully(bytes, 0L, bytes.size)
                            val chunkSize = 1024*512L
                            for(i in 0 until bytes.size step chunkSize)
                                writeFully(bytes, i, minOf(chunkSize, bytes.size - i))
                        }
                    }
//                    texts.entries.forEach {
//                        this.append(FormPart(it.key, it.value))
//                    }
                }
            )
        }
    }



    @DangerousInternalIoApi
    suspend fun uploadData3(data: FormData): String {
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

    suspend fun addShoppingListItem(spleeterJob: SpleeterJob) {
        println("addShoppingListItem()")
        jsonClient.post<Unit>(endpoint + SpleeterJob.path) {
            contentType(ContentType.Application.Json)
            body = spleeterJob
        }
    }

    suspend fun deleteJobListItem(spleeterJob: SpleeterJob) {
        println("deleteShoppingListItem()")
        jsonClient.delete<Unit>(endpoint + SpleeterJob.path + "/${spleeterJob.id}")
    }
}