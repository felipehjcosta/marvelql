package com.github.felipehjcosta.application

import com.github.felipehjcosta.domain.CharactersRepository
import com.github.felipehjcosta.domain.MarvelCharacter
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals

object QueryCharactersServiceTest : Spek({
    describe("execute service") {
        val fakeMarvelCharacter = MarvelCharacter(42L, "3-D Man", "Nice description!")
        val mockRepository by memoized { mockk<CharactersRepository>() }
        val service by memoized { QueryCharactersService(mockRepository) }

        afterEach {
            clearMocks(mockRepository)
        }

        context("with characters in Repository") {
            beforeEach {
                coEvery { mockRepository.fetchCharacters() } returns listOf(fakeMarvelCharacter)
            }

            it("should return characters") {
                val response = runBlocking { service.execute() }
                assertEquals(response.characters, listOf(fakeMarvelCharacter))
            }
        }
    }
})