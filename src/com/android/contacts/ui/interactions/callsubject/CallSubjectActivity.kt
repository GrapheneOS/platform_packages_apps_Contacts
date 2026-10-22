package com.android.contacts.ui.interactions.callsubject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telecom.TelecomManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.android.contacts.ui.core.AppTheme
import com.android.contacts.ui.interactions.callsubject.screen.CallSubjectDialog
import com.android.contacts.ui.interactions.callsubject.screen.CallSubjectEffectHandlerImpl
import com.android.contacts.ui.interactions.callsubject.screen.CallSubjectViewModel
import com.android.contacts.ui.interactions.callsubject.screen.model.CallSubjectArguments
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CallSubjectActivity : FragmentActivity() {

    @Inject
    lateinit var telecomManager: TelecomManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setHideOverlayWindows(true)

        val effectHandler = CallSubjectEffectHandlerImpl(
            activity = this,
            telecomManager = telecomManager,
        )

        setContent {
            AppTheme {
                CallSubjectDialog(
                    effectHandler = effectHandler,
                )
            }
        }
    }

    companion object {
        internal fun getIntent(context: Context, arguments: CallSubjectArguments): Intent {
            return Intent(context, CallSubjectActivity::class.java)
                .putExtra(CallSubjectViewModel.EXTRA_ARGUMENTS, arguments)
        }
    }
}
