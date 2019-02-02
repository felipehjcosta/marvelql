package com.github.felipehjcosta.application

import com.github.felipehjcosta.domain.CharactersRepository
import com.github.felipehjcosta.domain.MarvelCharacter

data class QueryCharactersResponse(val characters: List<MarvelCharacter>)

class QueryCharactersService(private val charactersRepository: CharactersRepository) {
    suspend fun execute(): QueryCharactersResponse = QueryCharactersResponse(charactersRepository.fetchCharacters())
}
