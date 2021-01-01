//@file:Suppress("UnsafeCastFromDynamic")
//
//import react.dom.render
//import kotlinx.browser.document
//
//@JsModule("react-hot-loader")
//private external val hotModule: dynamic
//private val hot = hotModule.hot
//private val module = js("module")
//
//fun main() {
//
//    println("in the main app, thats ok!")
//
//    val hotWrapper = hot(module)
//    render(document.getElementById("root")) {
////        hotWrapper(app())
//        hotWrapper(App)
//    }
//}