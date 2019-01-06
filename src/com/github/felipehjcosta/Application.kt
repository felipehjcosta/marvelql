package com.github.felipehjcosta

import com.github.pgutkowski.kgraphql.KGraphQL
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.routing
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import java.util.*

const val MILLISECONDS = 1000L

fun Application.module() {
    val schema = KGraphQL.schema {
        query("characters") {
            resolver<List<com.github.felipehjcosta.Character>> {
                val response = runBlocking { fetchCharacters() }
                println(">>> response: ${response}")
                response ?: emptyList()
            }
        }

        type<com.github.felipehjcosta.Character>()
    }
    install(ContentNegotiation) {
        gson()
    }
    routing {
        post("/graphql") {
            val query = call.receiveText()
            println("the graphql query: $query")

            call.respondText(schema.execute(query), ContentType.Application.Json)
        }
    }
}

private suspend fun fetchCharacters(): List<Character>? {
    val localTimestamp = (Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis / MILLISECONDS)
        .toString()
    val hashSignature = generateHash(localTimestamp)

    val client = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(JSON.nonstrict)
        }
    }

    val url = "https://gateway.marvel.com:443/v1/public/characters?" +
            "limit=10" +
            "&offset=0" +
            "&ts=${localTimestamp}" +
            "&apikey=${System.getenv("MARVEL_PUBLIC_KEY")}" +
            "&hash=${hashSignature}"
    println("Make request to ${url}")
    val response = client.get<CharacterDataWrapper> { url(url) }
    client.close()

    return response?.characterDataContainer?.characters
}

private fun generateHash(timestamp: String): String {
    return (timestamp + System.getenv("MARVEL_PRIVATE_KEY") + System.getenv("MARVEL_PUBLIC_KEY")).toMD5()
}

private fun String.toMD5(): String {
    val md = java.security.MessageDigest.getInstance("MD5")
    val digested = md.digest(toByteArray())
    return digested.joinToString("") { String.format("%02x", it) }
}

@Serializable
data class CharacterDataWrapper(
    @kotlinx.serialization.Optional @SerialName("code") var code: Int = 0,
    @kotlinx.serialization.Optional @SerialName("status") var status: String = "",
    @kotlinx.serialization.Optional @SerialName("data")
    var characterDataContainer: CharacterDataContainer = CharacterDataContainer()
)

@Serializable
class CharacterDataContainer(
    @kotlinx.serialization.Optional @SerialName("offset") var offset: Int = 0,
    @kotlinx.serialization.Optional @SerialName("limit") var limit: Int = 0,
    @kotlinx.serialization.Optional @SerialName("total") var total: Int = 0,
    @kotlinx.serialization.Optional @SerialName("count") var count: Int = 0,
    @kotlinx.serialization.Optional @SerialName("results") var characters: List<Character> = emptyList()
)

@Serializable
data class Character(
    @kotlinx.serialization.Optional @SerialName("id") var id: Long = -1,
    @kotlinx.serialization.Optional @SerialName("name") var name: String = "",
    @kotlinx.serialization.Optional @SerialName("description") var description: String = ""
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Character

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int = id.toInt()
}
