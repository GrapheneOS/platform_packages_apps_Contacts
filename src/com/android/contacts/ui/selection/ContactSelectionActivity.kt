package com.android.contacts.ui.selection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.android.contacts.activities.RequestPermissionsActivity
import com.android.contacts.di.core.MainImmediateDispatcher
import com.android.contacts.list.UiIntentActions
import com.android.contacts.ui.core.AppTheme
import com.android.contacts.ui.interactions.shortcut.BuildShortcutIntent
import com.android.contacts.ui.selection.screen.ContactSelectionEffectHandlerImpl
import com.android.contacts.ui.selection.screen.ContactSelectionScreen
import com.android.contacts.ui.selection.screen.mapper.ContactSelectionModeMapper
import com.android.contacts.ui.selection.screen.model.ContactSelectionRequest
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher

@AndroidEntryPoint
class ContactSelectionActivity : ComponentActivity() {

    @Inject
    internal lateinit var contactSelectionModeMapper: ContactSelectionModeMapper

    @Inject
    internal lateinit var buildShortcutIntent: BuildShortcutIntent

    @Inject
    @MainImmediateDispatcher
    internal lateinit var mainImmediateDispatcher: CoroutineDispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setHideOverlayWindows(true)
        enableEdgeToEdge()

        if (RequestPermissionsActivity.startPermissionActivityIfNeeded(this)) {
            return
        }

        val originalExtras = intent.extras?.deepCopy() ?: Bundle()
        val effectHandler = ContactSelectionEffectHandlerImpl(
            activity = this,
            originalExtras = originalExtras,
            buildShortcutIntent = buildShortcutIntent,
            mainDispatcher = mainImmediateDispatcher,
        )

        setContent {
            AppTheme {
                ContactSelectionScreen(
                    effectHandler = effectHandler,
                )
            }
        }
    }

    override val defaultViewModelCreationExtras: CreationExtras
        get() {
            return CreationExtras(super.defaultViewModelCreationExtras) {
                set(
                    DEFAULT_ARGS_KEY,
                    Bundle().apply { putParcelable(EXTRA_REQUEST, buildRequest()) },
                )
            }
        }

    private fun buildRequest(): ContactSelectionRequest? {
        // defaultViewModelCreationExtras can be called before injection, so this is a temporary
        // workaround until we refactor into a single activity app
        val mode = if (::contactSelectionModeMapper.isInitialized) {
            contactSelectionModeMapper.map(intent)
        } else {
            null
        } ?: return null
        return ContactSelectionRequest(
            mode = mode,
            titleOverride = intent.getStringExtra(UiIntentActions.TITLE_EXTRA_KEY),
        )
    }

    internal companion object {
        const val EXTRA_REQUEST = "request"
    }
}
