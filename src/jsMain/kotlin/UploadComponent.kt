import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MButtonSize
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.form.mFormControl
import com.ccfraser.muirwik.components.input.mInput
import com.ccfraser.muirwik.components.input.mInputLabel
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
//import com.example.core.network.api.ProfileAPI
import kotlinx.css.*
import kotlinx.html.InputType
import org.w3c.files.Blob
import org.w3c.files.FileReader
import react.*

interface UploadState : RState {
    var filePath: String?
    var fileBinaryString: String?
    var fileInput: String?
    var fileName: String?
}

external interface UploadProps: RProps {
    var onUploadComplete: suspend () -> Unit
}

class UploadComponent : RComponent<UploadProps, UploadState>() {
    private val fileReaderAsBinaryString = FileReader().apply {
        onload = {
            setState {
                fileBinaryString = it.target.asDynamic().result as? String
            }
        }
    }

    override fun UploadState.init() {
        filePath = ""
    }

    private fun save() {

        val trackFile = state.filePath
        println(trackFile)

        CoroutineScope(Dispatchers.Default).launch {
            Api.uploadData(state.fileName!!, state.fileBinaryString!!.toByteArray()) {
                println("result = $it")
                props.onUploadComplete()
            }
        }
    }

    override fun RBuilder.render() {
        mFormControl(fullWidth = true) {
            mInputLabel("Upload")
            mInput(type = InputType.file,
                    onChange = {
                        state.fileInput = it.targetInputValue
                        println(state.fileInput)

                        val file = it.target.asDynamic().files[0]
                        state.fileName = file.name as String
                        val blob = file as Blob
                        fileReaderAsBinaryString.readAsBinaryString(blob)
                    })
        }


        mButton(caption = "Upload",
//                primary = true,
                size = MButtonSize.medium,
//                fullWidth = true,
                onClick = { save() })
    }
}
