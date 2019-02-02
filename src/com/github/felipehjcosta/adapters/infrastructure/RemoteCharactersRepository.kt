package com.github.felipehjcosta.adapters.infrastructure

import com.github.felipehjcosta.domain.CharactersRepository
import com.github.felipehjcosta.domain.MarvelCharacter
import com.tripl3dogdare.havenjson.Json
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.get
import io.ktor.client.request.url
import java.time.Instant

class RemoteCharactersRepository(
    private val baseUrl: String,
    private val publicApiKey: String,
    private val privateApiKey: String
) : CharactersRepository {
    override suspend fun fetchCharacters(): List<MarvelCharacter> {

        val timestamp = Instant.now().toEpochMilli()
        val hash = generateHash(timestamp, publicApiKey, privateApiKey)

        val client = HttpClient(Apache)
        val url = "$baseUrl/v1/public/characters" +
                "?offset=0" +
                "&limit=10" +
                "&apikey=$publicApiKey" +
                "&ts=$timestamp" +
                "&hash=$hash"
        val response = client.get<String> { url(url) }
        client.close()


        return Json.parse(response).run {
            this["data"]["results"].asList?.map {
                MarvelCharacter(it["id"].asInt!!.toLong(), it["name"].asString!!, it["description"].asString!!)
            }
        } ?: emptyList()
    }

    private fun generateHash(timestamp: Long, publicApiKey: String, privateApiKey: String): String {
        return "$timestamp$privateApiKey$publicApiKey".toMD5()
    }
}
