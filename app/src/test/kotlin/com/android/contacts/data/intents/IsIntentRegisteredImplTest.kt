package com.android.contacts.data.intents

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class IsIntentRegisteredImplTest {

    private val packageManager = mockk<PackageManager>(relaxed = true)

    private val isIntentRegistered = IsIntentRegisteredImpl(packageManager = packageManager)

    @Test
    fun invoke_whenAnActivityHandlesTheIntent_returnsTrue() {
        givenMatches(ResolveInfo())

        assertTrue(isIntentRegistered(INTENT))
    }

    @Test
    fun invoke_whenNothingHandlesTheIntent_returnsFalse() {
        givenMatches()

        assertFalse(isIntentRegistered(INTENT))
    }

    private fun givenMatches(vararg matches: ResolveInfo) {
        every {
            packageManager.queryIntentActivities(any(), any<Int>())
        } returns matches.toList()
    }

    private companion object {
        val INTENT: Intent = Intent(Intent.ACTION_VIEW)
    }
}
