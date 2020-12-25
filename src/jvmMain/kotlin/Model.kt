import java.io.File

/**
 * Created by kirillkh on 12/23/2020.
 */
//sealed class SpleeterJobModel(val ts: Long, val type: String) {
//    class Success(val value: File, ts: Long) : SpleeterJobModel(ts)
//    class Failure(val reason: String, ts: Long) : SpleeterJobModel(ts)
//    class InProgress(val status: String, val filePath: String, ts: Long, rtjob: RuntimeJob): SpleeterJobModel(ts)
//}

enum class JobStatus {
    Success, Failure, InProgress
}

data class SpleeterJobModel(val ts: Long, val status: JobStatus,
                            val origFilePath: String,
                            val resultFilePath: String?,
                            val failureReason: String?)
