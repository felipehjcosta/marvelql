package com.github.felipehjcosta.adapters.infrastructure

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.github.felipehjcosta.domain.MarvelCharacter
import com.tripl3dogdare.havenjson.Json
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


object RemoteCharactersRepositoryTest : Spek({
    describe("RemoteCharactersRepository") {
        val mockWebServer = MockWebServer().apply { start() }
        val mockResult = this::class.java.classLoader.getResource("mock_result.json").readText()
        val mockCharacters = mockCharacters(Json.parse(mockResult))
        val baseUrl = "http://${mockWebServer.hostName}:${mockWebServer.port}"
        val publicApiKey = "MARVEL_API_KEY"
        val privateApiKey = "MARVEL_API_KEY"
        val remoteCharactersRepository = RemoteCharactersRepository(baseUrl, publicApiKey, privateApiKey)

        before {
            mockWebServer.setDispatcher(object : Dispatcher() {
                override fun dispatch(request: RecordedRequest?): MockResponse {
                    return testDispatcher(request, mockResult, privateApiKey)
                }
            })
        }

        after { mockWebServer.shutdown() }

        context("when fetch characters") {

            val characters by memoized { remoteCharactersRepository.fetchCharactersSynchronously() }

            it("should return 10 elements") {
                assertThat(characters).isNotNull().all { hasSize(10) }
            }

            it("should return parsed elements") {
                assertThat(characters).isEqualTo(mockCharacters)
            }
        }
    }
})

private fun RemoteCharactersRepository.fetchCharactersSynchronously(timeout: Long = 5000L): List<MarvelCharacter>? =
    runBlocking {
        withTimeoutOrNull(timeout) {
            fetchCharacters()
        }
    }

private fun mockCharacters(json: Json): List<MarvelCharacter> {
    return json["data"]["results"].asList!!.map {
        MarvelCharacter(it["id"].asInt!!.toLong(), it["name"].asString!!, it["description"].asString!!)
    }
}

private fun testDispatcher(request: RecordedRequest?, mockResult: String, privateApiKey: String): MockResponse {
    return if (request != null) {
        if (request.encodedPath() == "/v1/public/characters"
            && request.method == "GET"
            && request.queryParam("offset").toIntOrNull() == 0
            && request.queryParam("limit").toIntOrNull() == 10
            && request.queryParam("apikey").isNotBlank()
            && request.queryParam("ts").isNotBlank()
            && request.queryParam("hash") == (request.queryParam("ts") + privateApiKey + request.queryParam("apikey")).toMD5()
        ) {
            MockResponse().setResponseCode(200).setBody(mockResult)
        } else {
            MockResponse().setResponseCode(404)
        }
    } else {
        MockResponse().setResponseCode(404)
    }
}

fun RecordedRequest.encodedPath(): String = requestUrl.encodedPath()

fun RecordedRequest.queryParam(name: String): String = requestUrl.queryParameterValues(name).first()
