package com.github.felipehjcosta

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.tripl3dogdare.havenjson.Json
import java.util.*

class ApiClient {

    fun queryCharacters(query: String): Json {
        val (_, _, result) = "${Environment.baseUrl}/graphql".httpPost()
            .header("Authorization" to "Basic ${Base64.getEncoder().encodeToString("test:test".toByteArray())}")
            .timeout(5000)
            .body(query)
            .responseString()

        return Json.parse(result.get())
    }

    fun queryHealthCheck(): String {
        val (_, _, result) = "${Environment.baseUrl}/health".httpGet()
            .timeout(5000)
            .responseString()

        return result.get()
    }
}