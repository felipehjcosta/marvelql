package com.github.felipehjcosta

import assertk.assert
import assertk.assertions.isEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature

object HealthCheckFeatureTest : Spek({
    Feature("Health Check") {
        val apiClient by memoized { ApiClient() }
        var result: String? = null

        Scenario("Query Health Check") {
            When("Request Health Check") {
                result = apiClient.queryHealthCheck()
            }

            Then("it should contains OK") {
                assert(result).isEqualTo("OK")
            }
        }

    }

})
