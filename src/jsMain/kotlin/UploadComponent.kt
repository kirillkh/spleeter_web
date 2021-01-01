import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MButtonSize
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.form.mFormControl
import com.ccfraser.muirwik.components.input.mInput
import com.ccfraser.muirwik.components.input.mInputLabel
import io.ktor.client.call.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
//import com.example.core.network.api.ProfileAPI
import kotlinx.css.*
import kotlinx.html.InputType
import org.w3c.files.Blob
import org.w3c.files.FileReader
import react.*

private val scope = MainScope()

external interface UploadProps: RProps {
    var onUploadComplete: suspend () -> Unit
}

fun useStringState(v: String? = null) = useState(v)

val Upload = functionalComponent<UploadProps> { props ->
    var fileBinaryString: String? by useStringState()
    var fileInput: String? by useStringState()
    var fileName: String? by useStringState()

    val fileReaderAsBinaryString = FileReader().apply {
        onload = {
            fileBinaryString = it.target.asDynamic().result as? String
            null
        }
    }

    fun save() {
//        println(filePath)

        scope.launch {
            val response = Api.uploadData(fileName!!, fileBinaryString!!.toByteArray(), {})
            val result = response.call.receive<String>()
            println("result = $result")
            props.onUploadComplete()
        }
    }


    mFormControl(fullWidth = true) {
        mInputLabel("Upload")
        mInput(type = InputType.file,
            onChange = {
                fileInput = it.targetInputValue
                println(fileInput)

                val file = it.target.asDynamic().files[0]
                fileName = file?.name as String?
                val blob = file as Blob?
                if(blob == null)
                    fileBinaryString = null
                else
                    fileReaderAsBinaryString.readAsBinaryString(blob)
            })
    }


    mButton(caption = "Upload",
//                primary = true,
        size = MButtonSize.medium,
//                fullWidth = true,
        onClick = { save() })
}
