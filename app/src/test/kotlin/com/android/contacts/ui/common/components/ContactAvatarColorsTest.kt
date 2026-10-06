package com.android.contacts.ui.common.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class ContactAvatarColorsTest {

    @Test
    fun contactAvatarFallbackColors_forTheSameSeed_areStable() {
        val first = contactAvatarFallbackColors(colorSeed = "lookup-key", isDarkTheme = false)
        val second = contactAvatarFallbackColors(colorSeed = "lookup-key", isDarkTheme = false)

        assertEquals(first, second)
    }

    @Test
    fun contactAvatarFallbackColors_forDifferentSeeds_differ() {
        val first = contactAvatarFallbackColors(colorSeed = "anna", isDarkTheme = false)
        val second = contactAvatarFallbackColors(colorSeed = "boris", isDarkTheme = false)

        assertNotEquals(first.background, second.background)
    }

    @Test
    fun contactAvatarFallbackColors_forTheSameSeedInBothThemes_differ() {
        val light = contactAvatarFallbackColors(colorSeed = "anna", isDarkTheme = false)
        val dark = contactAvatarFallbackColors(colorSeed = "anna", isDarkTheme = true)

        assertNotEquals(light.background, dark.background)
    }

    @Test
    fun contactAvatarLabel_forALetterName_isTheUppercasedFirstLetter() {
        assertEquals("A", contactAvatarLabel(" anna smith"))
    }

    @Test
    fun contactAvatarLabel_forANonEnglishLetter_isTheUppercasedFirstLetter() {
        assertEquals("Ó", contactAvatarLabel("ólafur"))
    }

    @Test
    fun contactAvatarLabel_forAPhoneNumber_isNull() {
        assertNull(contactAvatarLabel("+1 555 0001"))
    }

    @Test
    fun contactAvatarLabel_forABlankName_isNull() {
        assertNull(contactAvatarLabel("   "))
    }

    @Test
    fun contactAvatarColorSeed_forABlankLookupKey_isNull() {
        assertNull(contactAvatarColorSeed(" "))
    }

    @Test
    fun contactAvatarColorSeed_forALookupKey_isTheKey() {
        assertEquals("lookup-key", contactAvatarColorSeed("lookup-key"))
    }
}
