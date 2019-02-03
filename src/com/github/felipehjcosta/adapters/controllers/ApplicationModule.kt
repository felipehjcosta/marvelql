package com.github.felipehjcosta.adapters.controllers

import com.github.felipehjcosta.adapters.infrastructure.RemoteCharactersRepository
import com.github.felipehjcosta.application.QueryCharactersService
import com.github.felipehjcosta.domain.CharactersRepository
import com.github.felipehjcosta.domain.MarvelCharacter
import com.github.pgutkowski.kgraphql.KGraphQL
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.installKoin

private const val BASE_URL = "https://gateway.marvel.com:443"
private val publicKey = System.getenv("MARVEL_PUBLIC_KEY")
private val privateKey = System.getenv("MARVEL_PRIVATE_KEY")

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
        single<CharactersRepository> {
            RemoteCharactersRepository(
                BASE_URL,
                publicKey,
                privateKey
            )
        }
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
}
