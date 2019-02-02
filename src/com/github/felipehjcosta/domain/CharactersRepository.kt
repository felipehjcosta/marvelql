package com.github.felipehjcosta.domain

interface CharactersRepository {
    suspend fun fetchCharacters(): List<MarvelCharacter>
}
