import com.benasher44.uuid.Uuid
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

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

object UuidSerializer: KSerializer<UuidWrapper> {
    override val descriptor: SerialDescriptor = LongArraySerializer().descriptor

    override fun serialize(encoder: Encoder, value: UuidWrapper) {
        val elems = longArrayOf(value.msb, value.lsb)
        encoder.encodeSerializableValue(LongArraySerializer(), elems)
    }

    override fun deserialize(decoder: Decoder): UuidWrapper {
        val elems = decoder.decodeSerializableValue(LongArraySerializer())
        return UuidWrapper(elems[1], elems[0])
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
