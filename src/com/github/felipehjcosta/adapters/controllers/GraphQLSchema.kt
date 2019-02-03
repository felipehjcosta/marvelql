package com.github.felipehjcosta.adapters.controllers

import com.github.felipehjcosta.application.QueryCharactersService
import com.github.felipehjcosta.domain.MarvelCharacter
import com.github.pgutkowski.kgraphql.KGraphQL
import com.github.pgutkowski.kgraphql.schema.Schema
import kotlinx.coroutines.runBlocking

const val CHARACTERS_QUERY_NAME = "characters"

fun createSchema(service: QueryCharactersService): Schema {
    return KGraphQL.schema {
        query(CHARACTERS_QUERY_NAME) {
            resolver<List<MarvelCharacter>> {
                val response = runBlocking {
                    service.execute()
                }
                println(">>> response: ${response.characters}")
                response.characters
            }
        }

        type<MarvelCharacter>()
    }
}
