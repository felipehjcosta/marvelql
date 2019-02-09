package com.github.felipehjcosta.adapters.controllers

import com.github.felipehjcosta.adapters.infrastructure.RemoteCharactersRepository
import com.github.felipehjcosta.application.QueryCharactersService
import com.github.felipehjcosta.domain.CharactersRepository
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.basic
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import org.koin.ktor.ext.installKoin

fun Application.module() {
    install(ContentNegotiation) {
        gson()
    }
    install(StatusPages) {
        exception<Throwable> {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
    val baseUrl = environment.config.property("ktor.marvel_gateway_url").getString()
    val publicKey = environment.config.property("ktor.marvel_gateway_key.public").getString()
    val privateKey = environment.config.property("ktor.marvel_gateway_key.private").getString()
    val module = org.koin.dsl.module {
        single<CharactersRepository> {
            RemoteCharactersRepository(baseUrl, publicKey, privateKey)
        }
        single { QueryCharactersService(get()) }
        single {
            createSchema(get())
        }
    }
    installKoin {
        modules(module)
    }
    install(Authentication) {
        basic {
            realm = "ktor"
            validate { credentials ->
                if (credentials.name == credentials.password) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }
}
