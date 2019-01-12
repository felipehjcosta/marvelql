package com.github.felipehjcosta

import assertk.assert
import assertk.assertions.hasSize
import assertk.assertions.isNotNull
import com.tripl3dogdare.havenjson.Json
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature

object ApiFeatureTest : Spek({
    Feature("GraphQL API") {
        val apiClient by memoized { ApiClient() }
        var result: Json? = null

        Scenario("Query characters") {
            When("Request characters") {
                result = apiClient.queryCharacters("{ characters { id name }}")
            }

            Then("it should contains characters with default size") {
                assert(result?.let { it["data"]["characters"].asList }).isNotNull {
                    it.hasSize(10)
                }
            }
        }

    }

})
