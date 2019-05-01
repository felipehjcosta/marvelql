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
import org.koin.core.module.Module
import org.koin.ktor.ext.installKoin

fun Application.module() {
    val baseUrl = environment.config.property("ktor.marvel_gateway_url").getString()
    val publicKey = environment.config.property("ktor.marvel_gateway_key.public").getString()
    val privateKey = environment.config.property("ktor.marvel_gateway_key.private").getString()

    val applicationModule = org.koin.dsl.module(createdAtStart = true) {
        single<CharactersRepository> { RemoteCharactersRepository(baseUrl, publicKey, privateKey) }
        single { QueryCharactersService(get()) }
    }
    moduleWithDependencies(applicationModule)
}

fun Application.moduleWithDependencies(module: Module) {
    installContentNegotiation()
    installStatusPages()
    installDependencyInjection(module)
    installAuthentication()
}

private fun Application.installContentNegotiation() {
    install(ContentNegotiation) {
        gson()
    }
}

private fun Application.installStatusPages() {
    install(StatusPages) {
        exception<Throwable> {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}

private fun Application.installDependencyInjection(module: Module) {
    installKoin {
        modules(module)
    }
}

private fun Application.installAuthentication() {
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
            skipWhen { isDevelopmentEnvironment }
        }
    }
}

val Application.envKind get() = environment.config.property("ktor.environment").getString()
val Application.isDevelopmentEnvironment get() = envKind == "dev"
