import io.ktor.http.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer

import kotlinx.browser.window

val endpoint = window.location.origin // only needed until https://github.com/ktorio/ktor/issues/1695 is resolved

val jsonClient = HttpClient {
    install(JsonFeature) { serializer = KotlinxSerializer() }
}

suspend fun getShoppingList(): List<SpleeterJob> {
    return jsonClient.get(endpoint + SpleeterJob.path)
}

suspend fun addShoppingListItem(spleeterJob: SpleeterJob) {
    jsonClient.post<Unit>(endpoint + SpleeterJob.path) {
        contentType(ContentType.Application.Json)
        body = spleeterJob
    }
}

suspend fun deleteShoppingListItem(spleeterJob: SpleeterJob) {
    jsonClient.delete<Unit>(endpoint + SpleeterJob.path + "/${spleeterJob.id}")
}