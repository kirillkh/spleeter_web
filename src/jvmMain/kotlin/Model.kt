import kotlinx.serialization.Serializable

/**
 * Created by kirillkh on 12/23/2020.
 */
//sealed class SpleeterJobModel(val ts: Long, val type: String) {
//    class Success(val value: File, ts: Long) : SpleeterJobModel(ts)
//    class Failure(val reason: String, ts: Long) : SpleeterJobModel(ts)
//    class InProgress(val status: String, val filePath: String, ts: Long, rtjob: RuntimeJob): SpleeterJobModel(ts)
//}

@Serializable
data class SpleeterJobModel(val id: UuidWrapper,
                            val ts: Long, val status: JobStatus,
                            val origFileName: String,
                            val srcFilePath: String,
                            val dstFilePath: String?,
                            val failureReason: String?)
