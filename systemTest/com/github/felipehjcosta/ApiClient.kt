package com.github.felipehjcosta

import com.github.kittinunf.fuel.httpPost
import com.tripl3dogdare.havenjson.Json

class ApiClient {

    fun queryCharacters(query: String): Json {
        val (_, _, result) = "${Environment.baseUrl}/graphql".httpPost()
            .timeout(5000)
            .body(query)
            .responseString()

        return Json.parse(result.get())
    }
}