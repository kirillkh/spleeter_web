import react.*
import react.dom.*
import kotlinext.js.*
import kotlinx.html.js.*
import kotlinx.coroutines.*

private val scope = MainScope()

val App = functionalComponent<RProps> { _ ->
    val (shoppingList, setShoppingList) = useState(emptyList<SpleeterJob>())

    useEffect(dependencies = listOf()) {
        scope.launch {
            setShoppingList(getShoppingList())
        }
    }

    h1 {
        +"Queue"
    }
    ul {
        shoppingList.sortedByDescending(SpleeterJob::priority).forEach { item ->
            li {
                key = item.toString()
                +"[${item.priority}] ${item.desc} "

                attrs.onClickFunction = {
                    scope.launch {
                        deleteShoppingListItem(item)
                        setShoppingList(getShoppingList())
                    }
                }
            }
        }
    }

    child(
        InputComponent,
        props = jsObject {
            onSubmit = { input ->
                val cartItem = SpleeterJob(input.replace("!", ""), input.count { it == '!' })
                scope.launch {
                    addShoppingListItem(cartItem)
                    setShoppingList(getShoppingList())
                }
            }
        }
    )
}