import kotlinx.serialization.Serializable

//@Serializable
//data class SpleeterJob(val origFile: String, val priority: Int) {
//    val id: Int = desc.hashCode()
//
//    companion object {
//        const val path = "/shoppingList"
//    }
//}




@Serializable
sealed class SpleeterJob(val ts: Long) {
    class Success(val uri: String, ts: Long) : SpleeterJob(ts)
    class Failure(val reason: String, ts: Long) : SpleeterJob(ts)
    class InProgress(val status: String, val fileName: String, ts: Long): SpleeterJob(ts)

    companion object {
        const val path = "/spleeter"
    }
}
