package com.github.felipehjcosta.adapters.controllers

import com.github.felipehjcosta.application.QueryCharactersResponse
import com.github.felipehjcosta.application.QueryCharactersService
import com.github.felipehjcosta.domain.MarvelCharacter
import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.*
import kotlin.test.assertEquals

object ApplicationTest : Spek({
    describe("Application") {
        val mockQueryCharactersService by memoized { mockk<QueryCharactersService>() }
        val modulesForTesting by memoized {
            module {
                single { mockQueryCharactersService }
            }
        }

        afterEach {
            clearMocks(mockQueryCharactersService)
            stopKoin()
        }
        withTestApplication({
            stubEnvironment()
            moduleWithDependencies(modulesForTesting)
            route()
        }) {

            context("when send a request to characters endpoint") {

                coEvery { mockQueryCharactersService.execute() } returns QueryCharactersResponse(
                    characters = listOf(
                        MarvelCharacter(42L, "3-D Man", "Nice description!")
                    )
                )

                context("in production environment") {
                    forceProductionEnvironment()

                    with(handleRequest(HttpMethod.Post, "graphql") {
                        addHeader(
                            "Authorization",
                            "Basic ${Base64.getEncoder().encodeToString("test:test".toByteArray())}"
                        )
                        setBody("{ characters { id name }}")
                    }) {

                        it("should respond 200") {
                            assertEquals(HttpStatusCode.OK, response.status())

                        }
                    }
                }

                context("in development environment") {
                    forceDevelpmentEnvironment()

                    with(handleRequest(HttpMethod.Post, "graphql") {
                        setBody("{ characters { id name }}")
                    }) {

                        it("should respond 200") {
                            assertEquals(HttpStatusCode.OK, response.status())
                        }
                    }
                }

            }

            context("when send a request to health check endpoint") {

                with(handleRequest(HttpMethod.Get, "health")) {

                    it("should respond 200") {
                        assertEquals(HttpStatusCode.OK, response.status())
                    }
                }

            }
        }
    }
})

private fun Application.stubEnvironment() {
    (environment.config as MapApplicationConfig).run {
        put("ktor.marvel_gateway_url", "")
        put("ktor.marvel_gateway_key.public", "")
        put("ktor.marvel_gateway_key.private", "")
    }
}

private fun TestApplicationEngine.forceProductionEnvironment() {
    (environment.config as MapApplicationConfig).run {
        put("ktor.environment", "prod")
    }
}

private fun TestApplicationEngine.forceDevelpmentEnvironment() {
    (environment.config as MapApplicationConfig).run {
        put("ktor.environment", "dev")
    }
}
