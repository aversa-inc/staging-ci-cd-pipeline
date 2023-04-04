package ca.aversa.insessionservice.util

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

internal class UtilsTest {

    private val notAllowedChars = setOf("+", "=", "\\/");

    @Test
    fun testCreateSecureId_EnsureSpecialCharactersArentPresent() {
        for(i in 1..100) {
            val secureId = Utils.createSecureId();

            notAllowedChars.forEach{character ->
                assertFalse(secureId.contains(character))
            }
        }
    }
}
