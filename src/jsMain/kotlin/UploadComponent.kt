import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.html.InputFormEncType
import kotlinx.html.InputFormMethod
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event
import org.w3c.files.Blob
import org.w3c.xhr.FormData
import react.PropsWithChildren
import react.dom.attrs
import react.dom.button
import react.dom.input
import react.fc
import react.useState

private val scope = MainScope()

external interface UploadProps: PropsWithChildren {
    var onUploadComplete: suspend () -> Unit
}

fun useStringState(v: String? = null) = useState(v)


val Event.targetInputValue: String
    get() = (target as? HTMLInputElement)?.value ?: (target as? HTMLTextAreaElement)?.value ?: ""

val Upload = fc<UploadProps> { props ->
    var fileName: String? by useStringState()
    var file: Blob? by useState(null)

    fun save() {
        scope.launch {
            val data = FormData()
            data.append(fileName!!, file!!)
            val result = Api.uploadData(data)

            println("result = $result")
            props.onUploadComplete()
        }
    }

    input(type = InputType.file, formEncType = InputFormEncType.multipartFormData, formMethod = InputFormMethod.post) {
        attrs.onChangeFunction =  {
//            println(it.targetInputValue)
            val f = it.target.asDynamic().files[0]
            file = f
            fileName = f?.name as String?
        }
    }

    button {
        attrs {
            text("Upload")
            onClickFunction = { save() }
        }
    }
}

fun Uint8Array.toHexString(): String {
    val b = StringBuilder(length)
    for (i in 0 until length) {
        b.append(this[i].toString(16).padStart(2, '0'))
    }
    return b.toString()
}
