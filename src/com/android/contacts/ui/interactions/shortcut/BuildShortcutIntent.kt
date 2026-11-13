package com.android.contacts.ui.interactions.shortcut

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.android.contacts.ShortcutIntentBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.suspendCancellableCoroutine

internal interface BuildShortcutIntent {
    suspend fun forContact(uri: Uri): Intent?
    suspend fun forCall(uri: Uri): Intent?
    suspend fun forMessage(uri: Uri): Intent?
}

internal class BuildShortcutIntentImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : BuildShortcutIntent {

    override suspend fun forContact(uri: Uri): Intent? {
        return withBuilder {
            createContactShortcutIntent(uri)
        }
    }

    override suspend fun forCall(uri: Uri): Intent? {
        return withBuilder {
            createPhoneNumberShortcutIntent(uri, Intent.ACTION_CALL)
        }
    }

    override suspend fun forMessage(uri: Uri): Intent? {
        return withBuilder {
            createPhoneNumberShortcutIntent(uri, Intent.ACTION_SENDTO)
        }
    }

    private suspend fun withBuilder(block: ShortcutIntentBuilder.() -> Unit): Intent? {
        return suspendCancellableCoroutine { cont ->
            ShortcutIntentBuilder(context) { _, intent ->
                cont.resume(
                    value = intent,
                    onCancellation = { e, _, _ ->
                        Log.w(TAG, "Shortcut build cancelled before it finished", e)
                    },
                )
            }.block()
        }
    }

    private companion object {
        const val TAG = "BuildShortcutIntent"
    }
}
