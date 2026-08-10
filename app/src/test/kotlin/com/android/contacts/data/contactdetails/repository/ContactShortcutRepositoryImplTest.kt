package com.android.contacts.data.contactdetails.repository

import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import com.android.contacts.DynamicShortcuts
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class ContactShortcutRepositoryImplTest {

    private val shortcutManager = mockk<ShortcutManager>(relaxed = true)
    private val dynamicShortcuts = mockk<DynamicShortcuts>()

    private val repository = ContactShortcutRepositoryImpl(
        shortcutManager = shortcutManager,
        dynamicShortcuts = dynamicShortcuts,
    )

    @Test
    fun isPinShortcutSupported_whenTheLauncherSupportsPinning_returnsTrue() {
        every { shortcutManager.isRequestPinShortcutSupported } returns true

        assertTrue(repository.isPinShortcutSupported())
    }

    @Test
    fun isPinShortcutSupported_whenTheLauncherDoesNotSupportPinning_returnsFalse() {
        every { shortcutManager.isRequestPinShortcutSupported } returns false

        assertFalse(repository.isPinShortcutSupported())
    }

    @Test
    fun requestPinShortcut_withAShortcutForTheContact_asksTheLauncherToPinIt() {
        val shortcutInfo = mockk<ShortcutInfo>()
        every {
            dynamicShortcuts.getQuickContactShortcutInfo(CONTACT_ID, LOOKUP_KEY, DISPLAY_NAME)
        } returns shortcutInfo

        repository.requestPinShortcut(
            contactId = CONTACT_ID,
            lookupKey = LOOKUP_KEY,
            displayName = DISPLAY_NAME,
        )

        verify { shortcutManager.requestPinShortcut(shortcutInfo, null) }
    }

    @Test
    fun requestPinShortcut_withoutAShortcutForTheContact_asksForNothing() {
        every {
            dynamicShortcuts.getQuickContactShortcutInfo(CONTACT_ID, LOOKUP_KEY, DISPLAY_NAME)
        } returns null

        repository.requestPinShortcut(
            contactId = CONTACT_ID,
            lookupKey = LOOKUP_KEY,
            displayName = DISPLAY_NAME,
        )

        verify(exactly = 0) { shortcutManager.requestPinShortcut(any(), any()) }
    }

    @Test
    fun reportShortcutUsed_withALookupKey_reportsItToTheLauncher() {
        repository.reportShortcutUsed(LOOKUP_KEY)

        verify { shortcutManager.reportShortcutUsed(LOOKUP_KEY) }
    }

    @Test
    fun reportShortcutUsed_withoutALookupKey_reportsNothing() {
        repository.reportShortcutUsed(null)

        verify(exactly = 0) { shortcutManager.reportShortcutUsed(any()) }
    }

    private companion object {
        const val CONTACT_ID = 7L
        const val LOOKUP_KEY = "lookup-key"
        const val DISPLAY_NAME = "Alex Doe"
    }
}
