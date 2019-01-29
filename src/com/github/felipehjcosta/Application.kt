@file:Suppress("MatchingDeclarationName")

package com.github.felipehjcosta

import com.github.felipehjcosta.domain.MarvelCharacter
import com.github.pgutkowski.kgraphql.KGraphQL
import com.tripl3dogdare.havenjson.Json
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import kotlinx.coroutines.runBlocking
import java.util.*

const val MILLISECONDS = 1000L

fun Application.module() {
    val schema = KGraphQL.schema {
        query("characters") {
            resolver<List<MarvelCharacter>> {
                val response = runBlocking { fetchCharacters() }
                println(">>> response: ${response}")
                response ?: emptyList()
            }
        }

        type<MarvelCharacter>()
    }
    install(ContentNegotiation) {
        gson()
    }
    install(StatusPages) {
        exception<Throwable> {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
    routing {
        post("/graphql") {
            val query = call.receiveText()
            println("the graphql query: $query")

            call.respondText(schema.execute(query), ContentType.Application.Json)
        }
        get("/health") {
            call.respondText("OK")
        }
    }
}

private suspend fun fetchCharacters(): List<MarvelCharacter>? {
    val localTimestamp = (Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis / MILLISECONDS)
        .toString()
    val hashSignature = generateHash(localTimestamp)

    val client = HttpClient(Apache)

    val url = "https://gateway.marvel.com:443/v1/public/characters?" +
            "limit=10" +
            "&offset=0" +
            "&ts=${localTimestamp}" +
            "&apikey=${System.getenv("MARVEL_PUBLIC_KEY")}" +
            "&hash=${hashSignature}"
    val response = client.get<String> { url(url) }
    client.close()

    return Json.parse(response).run {
        this["data"]["results"].asList?.map {
            MarvelCharacter(it["id"].asInt!!.toLong(), it["name"].asString!!, it["description"].asString!!)
        }
    }
}

private fun generateHash(timestamp: String): String {
    return (timestamp + System.getenv("MARVEL_PRIVATE_KEY") + System.getenv("MARVEL_PUBLIC_KEY")).toMD5()
}

private fun String.toMD5(): String {
    val md = java.security.MessageDigest.getInstance("MD5")
    val digested = md.digest(toByteArray())
    return digested.joinToString("") { String.format("%02x", it) }
}
