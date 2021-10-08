import com.benasher44.uuid.uuid4
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
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
import io.ktor.utils.io.core.*
import io.ktor.utils.io.nio.*
import kotlinx.coroutines.*
import org.bson.UuidRepresentation
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.*
import org.litote.kmongo.reactivestreams.KMongo
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.util.concurrent.Executors
import kotlin.collections.*
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.use

//import org.litote.kmongo.util.idValue

val MAX_JOBS = 10

val connectionString = System.getenv("MONGODB_URI")
val srcPath = System.getenv("SRC_PATH") ?: "/tmp/spleeter_web/src"
val dstPath = System.getenv("DST_PATH") ?: "/tmp/spleeter_web/dst"

val settings = MongoClientSettings.builder()
    .uuidRepresentation(UuidRepresentation.STANDARD)
    .applyConnectionString(ConnectionString(connectionString)).build()
val client = KMongo.createClient(settings).coroutine

val database = client.getDatabase("spleeter_web")
val collection = database.getCollection<SpleeterJobModel>()

//val bgJobContext = newFixedThreadPoolContext(2, "bgJobContext")
//val bgJobDispatcher = Executors.newFixedThreadPool(2).asCoroutineDispatcher()
val bgJobExecutor = Executors.newFixedThreadPool(2)

@ExperimentalPathApi
fun main() {
    onStart()

    embeddedServer(Netty, 9090) {
        install(ConditionalHeaders)
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
        install(StatusPages) {
            exception<Throwable> { cause ->
                cause.printStackTrace()
                println("------------------")
                call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
                throw cause
            }
        }

        routing {
            route(SpleeterJob.path) {
                get {
                    call.respond(jobsModelToUi())
                }
                post {
                    startJob()
                }
                delete("/{id}") {
                    val uuid = decodeUuid(call.parameters["id"]!!)
                    deleteJob(collection.findOne(SpleeterJobModel::id eq uuid)!!)
                    call.respond(HttpStatusCode.OK)
                }
                get("/d/{id}/{dlName}") {
                    val uuid = decodeUuid(call.parameters["id"]!!)
                    val job = collection.findOne(SpleeterJobModel::id eq uuid)!!
                    val file = File(job.dstFilePath!!)
                    if(file.exists()) {
                        call.response.header("Content-Disposition", "attachment")
                        call.respondFile(file)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
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
//                val file = File(uploadDir, "upload-${System.currentTimeMillis()}-${session.userId.hashCode()}-${title.hashCode()}.$ext")
                File(srcPath).mkdirs()
                val file = File(srcPath, "upload-${System.currentTimeMillis()}.$ext")
                part.streamProvider().use { input -> file.outputStream().buffered().use { output -> input.copyToSuspend(output) } }

                if(collection.countDocuments() >= MAX_JOBS)
                    collection.find()
                        .sort(SpleeterJobModel::ts eq 1)
                        .limit(1)
                        .first()?.let {
                            deleteJob(it)
                        }

                val job = SpleeterJobModel(
                    UuidWrapper(uuid4()), System.currentTimeMillis(), JobStatus.InProgress, part.originalFileName!!,
                    file.absolutePath, null, null)
                collection.insertOne(job)

                launchJob(job)
            }
        }

        part.dispose()
    }

    call.respond(HttpStatusCode.OK)
}

fun <T, U> List<T>.mapLet(block: T.()->U): List<U> = map {
    it.let(block)
}

fun decodeUuid(param: String): UuidWrapper {
    val encodedUuid = param.split(",")
        .map { it.toInt() }
        .toIntArray()
    return uuidFromIntArray(encodedUuid)
}

suspend fun jobsModelToUi(): List<SpleeterJob> =
    collection.find().toList().mapLet {
        SpleeterJob(id, ts, status, origFileName, dstFilePath, failureReason)
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
            File(job.srcFilePath).delete()
    }

    tryWithMsg {
        if(job.status == JobStatus.Success)
            File(job.dstFilePath!!).delete()
    }

    tryWithMsg {
        collection.deleteOne(SpleeterJobModel::id eq job.id)
    }
}

suspend fun cancelJob(job: SpleeterJobModel) {
    val copy = job.copy(status = JobStatus.Failure, failureReason = "restart")
    File(job.srcFilePath).delete()
//    job.idValue
//    val r = collection.replaceOneById(job.id, copy)
    val x = collection.find(SpleeterJobModel::id eq job.id).first()
    println("x=$x")
    val r = collection.replaceOne(SpleeterJobModel::id eq job.id, copy)
    println("r = $r")
}

@ExperimentalPathApi
fun onStart() {
    Files.createDirectories(Path(srcPath))
    Files.createDirectories(Path(dstPath))

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



fun launchJob(job: SpleeterJobModel) {
    bgJobExecutor.execute {
        val src = File(job.srcFilePath)
        val fileNameBase = src.nameWithoutExtension
        val resultBase = "$dstPath/$fileNameBase"
        val accompanimentResult = "$resultBase/accompaniment.wav"
        val vocalsResult = "$resultBase/vocals.wav"
        val result = File("$dstPath/$fileNameBase-dst.mp3")
        try {
            val pb = ProcessBuilder(
                "/usr/bin/spleeter", "separate",
                src.absolutePath,
                "-p", "spleeter:2stems",
                "-o", dstPath
            )
            println("DEBUG: starting process: " + pb.command().joinToString(" "))
            val proc = pb.start()
            val spleeterResult = proc.waitFor()
            if(spleeterResult != 0)
                println("ERROR: spleeter JOB failed with $spleeterResult")
            if(!File(accompanimentResult).exists())
                println("ERROR: $accompanimentResult does not exist after spleeter")

            val pb2 = ProcessBuilder(
                "/usr/bin/ffmpeg", "-i", accompanimentResult, "-qscale:a", "0", result.absolutePath
            )
            println("DEBUG: starting process: " + pb2.command().joinToString(" "))
            val proc2 = pb2.start()
            val parallelResult = proc2.waitFor()
            if(parallelResult != 0)
                println("ERROR: parallel JOB failed with $parallelResult")
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            try {
                File(accompanimentResult).delete()
                File(vocalsResult).delete()
                File(resultBase).delete()
            } finally {
            }
        }

        val status = if (result.exists()) JobStatus.Success else JobStatus.Failure

        runBlocking {
            if (collection.findOne(SpleeterJobModel::id eq job.id) != null) {
                collection.replaceOne(
                    SpleeterJobModel::id eq job.id,
                    job.copy(status = status, dstFilePath = result.absolutePath)
                )
            } else {
                result.delete()
            }
        }
    }

    println("----------------------------- launched job!")
}
