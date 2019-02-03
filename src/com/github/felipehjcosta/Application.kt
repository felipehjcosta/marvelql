@file:Suppress("MatchingDeclarationName")

package com.github.felipehjcosta

import com.github.felipehjcosta.adapters.infrastructure.RemoteCharactersRepository
import com.github.felipehjcosta.application.QueryCharactersService
import com.github.felipehjcosta.domain.CharactersRepository
import com.github.felipehjcosta.domain.MarvelCharacter
import com.github.pgutkowski.kgraphql.KGraphQL
import com.github.pgutkowski.kgraphql.schema.Schema
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
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
import org.koin.ktor.ext.inject
import org.koin.ktor.ext.installKoin

const val BASE_URL = "https://gateway.marvel.com:443"
val PUBLIC_KEY = System.getenv("MARVEL_PUBLIC_KEY")
val PRIVATE_KEY = System.getenv("MARVEL_PRIVATE_KEY")

fun Application.module() {
    install(ContentNegotiation) {
        gson()
    }
    install(StatusPages) {
        exception<Throwable> {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
    val module = org.koin.dsl.module {
        single<CharactersRepository> { RemoteCharactersRepository(BASE_URL, PUBLIC_KEY, PRIVATE_KEY) }
        single { QueryCharactersService(get()) }
        single {
            KGraphQL.schema {
                query("characters") {
                    resolver<List<MarvelCharacter>> {
                        val response = runBlocking {
                            get<QueryCharactersService>().execute()
                        }
                        println(">>> response: ${response.characters}")
                        response.characters
                    }
                }

                type<MarvelCharacter>()
            }
        }
    }
    installKoin {
        modules(module)
    }
    val schema: Schema by inject()
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
