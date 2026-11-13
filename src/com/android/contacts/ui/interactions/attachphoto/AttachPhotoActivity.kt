package com.android.contacts.ui.interactions.attachphoto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.CreationExtras
import com.android.contacts.activities.RequestPermissionsActivity
import com.android.contacts.ui.interactions.attachphoto.screen.AttachPhotoEffectHandlerImpl
import com.android.contacts.ui.interactions.attachphoto.screen.AttachPhotoViewModel
import com.android.contacts.ui.interactions.attachphoto.screen.model.AttachPhotoAction as Action
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AttachPhotoActivity : ComponentActivity() {

    private val viewModel by viewModels<AttachPhotoViewModel>()

    private val pickContactLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val contactUri = result.data?.data?.takeIf { result.resultCode == RESULT_OK }
        viewModel.onAction(Action.ContactPicked(contactUri))
    }

    private val cropPhotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        viewModel.onAction(Action.CropFinished(isSuccess = result.resultCode == RESULT_OK))
    }

    private val pickDefaultAccountLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        viewModel.onAction(Action.DefaultAccountPicked)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (RequestPermissionsActivity.startPermissionActivityIfNeeded(this)) {
            return
        }

        val effectHandler = AttachPhotoEffectHandlerImpl(
            activity = this,
            pickContactLauncher = pickContactLauncher,
            cropPhotoLauncher = cropPhotoLauncher,
            pickDefaultAccountLauncher = pickDefaultAccountLauncher,
            onAction = viewModel::onAction,
        )

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect(effectHandler::handle)
            }
        }
    }

    override val defaultViewModelCreationExtras: CreationExtras
        get() {
            return CreationExtras(super.defaultViewModelCreationExtras) {
                set(
                    DEFAULT_ARGS_KEY,
                    Bundle().apply {
                        putParcelable(EXTRA_SOURCE_URI, intent.data)
                        putString(EXTRA_MIME_TYPE, intent.getStringExtra(EXTRA_MIME_TYPE))
                    },
                )
            }
        }

    companion object {
        internal const val EXTRA_SOURCE_URI = "sourceUri"
        internal const val EXTRA_MIME_TYPE = "mimeType"
    }
}
