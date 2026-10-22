package com.android.contacts.util.core.extension

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class StringExtensionsTest {

    @Test
    fun limit_withoutEncoding_forAShorterString_isUnchanged() {
        assertEquals("hello", "hello".limit(limit = 16, encoding = null))
    }

    @Test
    fun limit_withoutEncoding_forAStringAtTheLimit_isUnchanged() {
        assertEquals("hello", "hello".limit(limit = 5, encoding = null))
    }

    @Test
    fun limit_withoutEncoding_forALongerString_isTruncatedToTheLimit() {
        assertEquals("hel", "hello".limit(limit = 3, encoding = null))
    }

    @Test
    fun limit_withoutEncoding_countsCharactersNotBytes() {
        assertEquals("éé", "ééé".limit(limit = 2, encoding = null))
    }

    @Test
    fun limit_withUtf8_countsBytesNotCharacters() {
        // "é" is 2 bytes in UTF-8
        assertEquals("éé", "ééé".limit(limit = 4, encoding = Charsets.UTF_8))
    }

    @Test
    fun limit_withUtf8_doesNotSplitAMultiByteCharacter() {
        assertEquals("a", "aé".limit(limit = 2, encoding = Charsets.UTF_8))
    }
}
