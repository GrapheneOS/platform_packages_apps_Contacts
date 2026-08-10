package com.android.contacts.data.contactdetails.repository

import android.content.pm.ShortcutManager
import com.android.contacts.DynamicShortcuts
import javax.inject.Inject

internal interface ContactShortcutRepository {

    fun isPinShortcutSupported(): Boolean

    fun requestPinShortcut(
        contactId: Long,
        lookupKey: String?,
        displayName: String?,
    )

    fun reportShortcutUsed(lookupKey: String?)
}

internal class ContactShortcutRepositoryImpl @Inject constructor(
    private val shortcutManager: ShortcutManager,
    private val dynamicShortcuts: DynamicShortcuts,
) : ContactShortcutRepository {

    override fun isPinShortcutSupported(): Boolean {
        return shortcutManager.isRequestPinShortcutSupported
    }

    override fun requestPinShortcut(
        contactId: Long,
        lookupKey: String?,
        displayName: String?,
    ) {
        val shortcutInfo = dynamicShortcuts.getQuickContactShortcutInfo(
            contactId,
            lookupKey,
            displayName,
        ) ?: return

        shortcutManager.requestPinShortcut(shortcutInfo, null)
    }

    override fun reportShortcutUsed(lookupKey: String?) {
        if (lookupKey == null) {
            return
        }

        shortcutManager.reportShortcutUsed(lookupKey)
    }
}
