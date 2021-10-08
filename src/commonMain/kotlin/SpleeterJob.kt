import com.benasher44.uuid.Uuid
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
enum class JobStatus {
    Success, Failure, InProgress
}

// Required in order to BSON to work correctly (otherwise it serializes UUID via some other mechanism)
@Serializable(UuidSerializer::class)
data class UuidWrapper(val id: Uuid) {
    val msb: Long
        get() = id.mostSignificantBits
    val lsb: Long
        get() = id.leastSignificantBits

    constructor(msb: Long, lsb: Long) : this(Uuid(msb, lsb))
}

fun encodeLong(x: Long) = intArrayOf((x ushr 32).toInt(), x.toInt())
fun decodeLong(x0: Int, x1: Int) = (x0.toLong() shl 32) or (x1.toLong() and ((1L shl 32)-1))

fun uuidToIntArray(u: UuidWrapper) = encodeLong(u.msb) + encodeLong(u.lsb)
fun uuidFromIntArray(x: IntArray) = UuidWrapper(decodeLong(x[0], x[1]), decodeLong(x[2], x[3]))

object UuidSerializer: KSerializer<UuidWrapper> {
    override val descriptor: SerialDescriptor = IntArraySerializer().descriptor

    override fun serialize(encoder: Encoder, value: UuidWrapper) {
        val elems = uuidToIntArray(value)
        encoder.encodeSerializableValue(IntArraySerializer(), elems)
    }

    override fun deserialize(decoder: Decoder): UuidWrapper {
        val x = decoder.decodeSerializableValue(IntArraySerializer())
        return uuidFromIntArray(x)
    }
}


@Serializable
data class SpleeterJob(val id: UuidWrapper,
                       val ts: Long, val status: JobStatus,
                       val origFileName: String,
                       val resultUri: String?,
                       val failureReason: String?) {
    companion object {
        const val path = "/spleeter"
    }
}
