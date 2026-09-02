package com.android.contacts.ui.interactions.showorcreate

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.android.contacts.ui.core.AppTheme
import com.android.contacts.ui.interactions.showorcreate.screen.ShowOrCreateDialog
import com.android.contacts.ui.interactions.showorcreate.screen.ShowOrCreateEffectHandlerImpl
import com.android.contacts.ui.interactions.showorcreate.screen.ShowOrCreateViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShowOrCreateActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        intent.putExtra(ShowOrCreateViewModel.EXTRA_DATA, intent.data)
        intent.putExtra(ShowOrCreateViewModel.EXTRA_EXTRAS, intent.extras)

        val effectHandler = ShowOrCreateEffectHandlerImpl(
            activity = this,
        )

        setContent {
            AppTheme {
                ShowOrCreateDialog(
                    effectHandler = effectHandler,
                )
            }
        }
    }
}
