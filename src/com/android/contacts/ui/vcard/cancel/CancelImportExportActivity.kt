package com.android.contacts.ui.vcard.cancel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.android.contacts.ui.core.AppTheme
import com.android.contacts.ui.vcard.cancel.screen.CancelImportExportDialog
import com.android.contacts.ui.vcard.cancel.screen.CancelImportExportEffectHandlerImpl
import com.android.contacts.ui.vcard.cancel.screen.CancelImportExportViewModel
import com.android.contacts.ui.vcard.cancel.screen.model.VCardOperationType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CancelImportExportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val effectHandler = CancelImportExportEffectHandlerImpl(
            activity = this,
        )

        setContent {
            AppTheme {
                CancelImportExportDialog(
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
                    Bundle().apply {
                        putParcelable(CancelImportExportViewModel.EXTRA_DATA, intent.data)
                    },
                )
            }
        }

    internal companion object {
        fun getIntent(
            context: Context,
            jobId: Int,
            displayName: String,
            type: VCardOperationType,
        ): Intent {
            return Intent(context, CancelImportExportActivity::class.java)
                .setData(buildUri(jobId, displayName, type))
        }

        // Note: We cannot use extra values here (like setIntExtra()), as PendingIntent doesn't
        // preserve them across multiple Notifications. PendingIntent preserves the first extras
        // (when flag is not set), or update them when PendingIntent#getActivity() is called
        // (See PendingIntent#FLAG_UPDATE_CURRENT). In either case, we cannot preserve extras as we
        // expect (for each vCard import/export request).
        //
        // We use query parameter in Uri instead.
        // Scheme and Authority is arbitorary, assuming we never refer them.
        @VisibleForTesting
        fun buildUri(
            jobId: Int,
            displayName: String,
            type: VCardOperationType,
        ): Uri {
            return Uri.Builder()
                .scheme("invalidscheme")
                .authority("invalidauthority")
                .appendQueryParameter(
                    CancelImportExportViewModel.QUERY_PARAM_JOB_ID,
                    jobId.toString(),
                )
                .appendQueryParameter(
                    CancelImportExportViewModel.QUERY_PARAM_DISPLAY_NAME,
                    displayName,
                )
                .appendQueryParameter(
                    CancelImportExportViewModel.QUERY_PARAM_TYPE,
                    type.value,
                ).build()
        }
    }
}
