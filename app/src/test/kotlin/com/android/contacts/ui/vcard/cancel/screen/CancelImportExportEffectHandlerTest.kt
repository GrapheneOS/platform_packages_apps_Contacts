package com.android.contacts.ui.vcard.cancel.screen

import android.app.Activity
import com.android.contacts.ui.vcard.cancel.screen.model.CancelImportExportEffect as Effect
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class CancelImportExportEffectHandlerTest {

    private val activity = mockk<Activity>(relaxed = true)

    private val effectHandler: CancelImportExportEffectHandler =
        CancelImportExportEffectHandlerImpl(
            activity = activity,
        )

    @Test
    fun onClose_finishesActivity() {
        effectHandler.handle(Effect.CLOSE)

        verify { activity.finish() }
    }
}
