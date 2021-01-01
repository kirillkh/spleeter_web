//import com.ccfraser.muirwik.components.*
//import com.ccfraser.muirwik.components.button.MButtonSize
//import com.ccfraser.muirwik.components.button.mButton
//import com.ccfraser.muirwik.components.form.mFormControl
//import com.ccfraser.muirwik.components.input.mInput
//import com.ccfraser.muirwik.components.input.mInputLabel
import io.ktor.client.call.*
import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.internal.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
//import com.example.core.network.api.ProfileAPI
import kotlinx.html.InputFormEncType
import kotlinx.html.InputFormMethod
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.DataView
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event
import org.w3c.files.Blob
import org.w3c.files.FileReader
import org.w3c.xhr.FormData
import react.*
import react.dom.button
import react.dom.input

private val scope = MainScope()

external interface UploadProps: RProps {
    var onUploadComplete: suspend () -> Unit
}

@ExperimentalUnsignedTypes
fun ByteArray.toHexString() = asUByteArray().joinToString("") {
    it.toString(16).padStart(2, '0')
}

fun Uint8Array.toHexString(): String {
    val b = StringBuilder(length)
    for (i in 0 until length) {
        b.append(this[i].toString(16).padStart(2, '0'))
    }
    return b.toString()
}

fun useStringState(v: String? = null) = useState(v)


val Event.targetInputValue: String
    get() = (target as? HTMLInputElement)?.value ?: (target as? HTMLTextAreaElement)?.value ?: ""

@OptIn(DangerousInternalIoApi::class)
val Upload = functionalComponent<UploadProps> { props ->
    var fileUrl: String? by useStringState()
    var fileBinaryString: String? by useStringState()
    var fileArrayBuffer: ArrayBuffer? by useState(null as ArrayBuffer?)
    var fileInput: String? by useStringState()
    var fileName: String? by useStringState()
    var file: Blob? by useState(null)

    val fileReaderAsArrayBuffer = FileReader().apply {
        onload = {
            val result = it.target.asDynamic().result.unsafeCast<ArrayBuffer>()
            fileArrayBuffer = result
//            println("fileArrayBuffer: $result")
            null
        }
    }

    val fileReaderAsBinaryString = FileReader().apply {
        onload = {
            val result = it.target.asDynamic().result as? String
            fileBinaryString = result
//            println("fileBinaryString: $result")
            null
        }
    }

    val fileReaderAsUrl = FileReader().apply {
        onload = {
            val x = it.target.asDynamic().result as? String
            fileUrl = x
//            println("fileUrl: $x")
            null
        }
    }

    fun save() {
//        println(filePath)

        scope.launch {
            val x = Uint8Array(fileArrayBuffer!!)
//            val x = Uint8ArrayConstructor.from(fileArrayBuffer!!) as Uint8Array
//            println("x=${x.toHexString()}")
//            x.toByteArray()

            val y = Memory(DataView(fileArrayBuffer!!))

//            val response = Api.uploadData(fileName!!, fileBinaryString!!.toByteArray())

//            val response = Api.uploadData2(fileName!!, y)
//            val result = response.call.receive<String>()

            val data = FormData()
            data.append(fileName!!, file!!)
            val result = Api.uploadData3(data)

//            val result = response.call.receive<String>()
            println("result = $result")
            props.onUploadComplete()
        }
    }

    input(type = InputType.file, formEncType = InputFormEncType.multipartFormData, formMethod = InputFormMethod.post) {
        attrs.onChangeFunction =  {
            fileInput = it.targetInputValue
            println(fileInput)

            val f = it.target.asDynamic().files[0]
            file = f
            fileName = f?.name as String?
            val blob = f as Blob?
            if(blob == null) {
                file = null
                fileBinaryString = null
            } else {
                fileReaderAsBinaryString.readAsBinaryString(blob)
                fileReaderAsUrl.readAsDataURL(blob)
                fileReaderAsArrayBuffer.readAsArrayBuffer(blob)
            }
        }
    }

    button {
        attrs {
            text("Upload")
            onClickFunction = { save() }
        }

    }

//    mFormControl(fullWidth = true) {
//        mInputLabel("Upload")
//        mInput(type = InputType.file,
//            onChange = {
//                fileInput = it.targetInputValue
//                println(fileInput)
//
//                val file = it.target.asDynamic().files[0]
//                fileName = file?.name as String?
//                val blob = file as Blob?
//                if(blob == null)
//                    fileBinaryString = null
//                else
//                    fileReaderAsBinaryString.readAsBinaryString(blob)
//            })
//    }


//    mButton(caption = "Upload",
////                primary = true,
//        size = MButtonSize.medium,
////                fullWidth = true,
//        onClick = { save() })
}
