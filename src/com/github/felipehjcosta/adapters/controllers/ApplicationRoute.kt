@file:Suppress("MatchingDeclarationName")

package com.github.felipehjcosta.adapters.controllers

import com.github.felipehjcosta.application.QueryCharactersService
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.ContentType
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import org.koin.ktor.ext.inject

fun Application.route() {
    val service: QueryCharactersService by inject()
    routing {
        authenticate {
            post("/graphql") {
                val query = call.receiveText()
                println("the graphql query: $query")

                call.respondText(createSchema(service).execute(query), ContentType.Application.Json)
            }
        }
        get("/health") {
            call.respondText("OK")
        }
    }
}
