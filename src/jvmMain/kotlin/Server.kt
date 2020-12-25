import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*

import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.pipeline.*
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.*
import org.litote.kmongo.reactivestreams.KMongo
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlin.collections.*
import com.mongodb.client.MongoIterable
import kotlinx.coroutines.*
import org.litote.kmongo.util.idValue
import kotlin.collections.max

//val shoppingList = mutableListOf(
//    ShoppingListItem("Cucumbers 🥒", 1),
//    ShoppingListItem("Tomatoes 🍅", 2),
//    ShoppingListItem("Orange Juice 🍊", 3)
//)

val MAX_JOBS = 10

val connectionString = System.getenv("MONGODB_URI")
    ?: "mongodb://admin:Y.Zhy!jl766gnJlz@127.0.0.1"

//val client = KMongo.createClient("mongodb://admin:Y.Zhy!jl766gnJlz@127.0.0.1").coroutine
val client = KMongo.createClient(connectionString).coroutine

val database = client.getDatabase("shoppingList")
val collection = database.getCollection<SpleeterJobModel>()

fun main() {
    onStart();

    embeddedServer(Netty, 9090) {
        install(CachingHeaders) {
            options { outgoingContent ->
                val cachingOptions = CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60))
                when (outgoingContent.contentType?.withoutParameters()) {
                    ContentType.Text.CSS -> cachingOptions
//                    ContentType.Text.JavaScript -> cachingOptions
                    ContentType.Application.JavaScript -> cachingOptions
                    ContentType.Image.Any -> cachingOptions
                    else -> null
                }
            }
        }

        install(ContentNegotiation) {
            json()
        }
        install(CORS) {
            method(HttpMethod.Get)
            method(HttpMethod.Post)
            method(HttpMethod.Delete)
            anyHost()
        }
        install(Compression) {
            gzip()
        }

        routing {
            route(SpleeterJob.path) {
//                get {
//                    call.respond(shoppingList)
//                }
//                post {
//                    shoppingList += call.receive<ShoppingListItem>()
//                    call.respond(HttpStatusCode.OK)
//                }
//                delete("/{id}") {
//                    val id = call.parameters["id"]?.toInt() ?: error("Invalid delete request")
//                    shoppingList.removeIf { it.id == id }
//                    call.respond(HttpStatusCode.OK)
//                }

                get {
                    call.respond(collection.find().toList())
                }
                post {
                    startJob()
                }
                delete("/{id}") {
                    val id = call.parameters["id"]?.toInt() ?: error("Invalid delete request")
                    collection.deleteOne(SpleeterJobModel::idValue eq id)
                    call.respond(HttpStatusCode.OK)
                }
            }

            get("/") {
                call.respondText(
                    this::class.java.classLoader.getResource("index.html")!!.readText(),
                    ContentType.Text.Html
                )
            }
            static("/") {
                resources("")
            }
        }
    }.start(wait = true)
}

suspend fun PipelineContext<Unit, ApplicationCall>.startJob() {
//    collection.insertOne(call.receive<SpleeterJob>())
    val multipart = call.receiveMultipart()
    multipart.forEachPart { part ->
        when (part) {
//            is PartData.FormItem -> {
//                if (part.name == "title") {
//                    title = part.value
//                }
//            }
            is PartData.FileItem -> {
                val ext = File(part.originalFileName!!).extension
                val uploadDir = "/tmp/asdf"
//                val file = File(uploadDir, "upload-${System.currentTimeMillis()}-${session.userId.hashCode()}-${title.hashCode()}.$ext")
                val file = File(uploadDir, "upload-${System.currentTimeMillis()}}.$ext")
                part.streamProvider().use { input -> file.outputStream().buffered().use { output -> input.copyToSuspend(output) } }

                if(collection.countDocuments() >= MAX_JOBS)
                    collection.find()
                        .sort(SpleeterJobModel::ts eq 1)
                        .limit(1)
                        .first()?.let {
                            deleteJob(it)
                        }

                val job = SpleeterJobModel(System.currentTimeMillis(), JobStatus.InProgress, file.absolutePath, null, null)
                collection.insertOne(job)
            }
        }

        part.dispose()
    }

    call.respond(HttpStatusCode.OK)
}

suspend fun tryWithMsg(block: suspend () -> Unit) {
    try {
        block()
    } catch(e: Exception) {
        e.printStackTrace()
    }
}

suspend fun deleteJob(job: SpleeterJobModel) {
    tryWithMsg {
        if (job.status == JobStatus.InProgress)
            cancelJob(job)
        else
            File(job.origFilePath).delete()
    }

    tryWithMsg {
        if(job.status == JobStatus.Success)
            File(job.resultFilePath!!).delete()
    }

    tryWithMsg {
        collection.deleteOneById(job.idValue!!)
    }
}

suspend fun cancelJob(job: SpleeterJobModel) {
    val copy = job.copy(status = JobStatus.Failure, failureReason = "restart")
    File(job.origFilePath).delete()
    collection.replaceOneById(job.idValue!!, copy)
}

fun onStart() {
//    collection.deleteMany()
    runBlocking {
        collection
            .find(SpleeterJobModel::status eq JobStatus.InProgress)
            .consumeEach {
                cancelJob(it)
            }
    }
}


//fun <TResult>MongoIterable<TResult>.max(): SpleeterJob {
//    return useCursor { it.max() }
//}

suspend fun InputStream.copyToSuspend(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    yieldSize: Int = 4 * 1024 * 1024,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): Long {
    return withContext(dispatcher) {
        val buffer = ByteArray(bufferSize)
        var bytesCopied = 0L
        var bytesAfterYield = 0L
        while (true) {
            val bytes = read(buffer).takeIf { it >= 0 } ?: break
            out.write(buffer, 0, bytes)
            if (bytesAfterYield >= yieldSize) {
                yield()
                bytesAfterYield %= yieldSize
            }
            bytesCopied += bytes
            bytesAfterYield += bytes
        }
        return@withContext bytesCopied
    }
}