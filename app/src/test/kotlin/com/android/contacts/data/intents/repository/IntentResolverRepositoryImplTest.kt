package com.android.contacts.data.intents.repository

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class IntentResolverRepositoryImplTest {

    private val packageManager = mockk<PackageManager>(relaxed = true)

    private val repository = IntentResolverRepositoryImpl(packageManager = packageManager)

    @Before
    fun setUp() {
        givenMatches()
        givenResolvedActivity(null)
    }

    @Test
    fun isIntentRegistered_whenAnActivityHandlesTheIntent_returnsTrue() {
        givenMatches(resolveInfo(packageName = "com.example.dialer", label = "Dialer"))

        assertTrue(repository.isIntentRegistered(INTENT))
    }

    @Test
    fun isIntentRegistered_whenNothingHandlesTheIntent_returnsFalse() {
        givenMatches()

        assertFalse(repository.isIntentRegistered(INTENT))
    }

    @Test
    fun resolveLabel_whenNothingHandlesTheIntent_returnsNull() {
        givenMatches()

        assertNull(repository.resolveLabel(INTENT))
    }

    @Test
    fun resolveLabel_withASingleMatch_returnsItsLabel() {
        givenMatches(resolveInfo(packageName = "com.example.dialer", label = "Dialer"))

        assertEquals("Dialer", repository.resolveLabel(INTENT))
    }

    @Test
    fun resolveLabel_whenTheSystemResolvesTheIntent_returnsThatLabel() {
        givenMatches(
            resolveInfo(packageName = "com.example.first", label = "First"),
            resolveInfo(packageName = "com.example.second", label = "Second"),
        )
        givenResolvedActivity(
            resolveInfo(
                packageName = "com.example.default",
                label = "Default",
                isConcreteMatch = true,
            ),
        )

        assertEquals("Default", repository.resolveLabel(INTENT))
    }

    @Test
    fun resolveLabel_whenTheSystemOffersTheDisambiguationDialog_prefersAKnownPackage() {
        givenMatches(
            resolveInfo(packageName = "com.example.first", label = "First"),
            resolveInfo(packageName = "com.android.chrome", label = "Chrome"),
        )
        givenResolvedActivity(resolveInfo(packageName = "android", label = "Chooser"))

        assertEquals("Chrome", repository.resolveLabel(INTENT))
    }

    @Test
    fun resolveLabel_whenNothingIsResolved_prefersAKnownPackage() {
        givenMatches(
            resolveInfo(packageName = "com.example.first", label = "First", isSystem = true),
            resolveInfo(packageName = "com.android.chrome", label = "Chrome"),
        )

        assertEquals("Chrome", repository.resolveLabel(INTENT))
    }

    @Test
    fun resolveLabel_withoutAKnownPackage_prefersASystemApplication() {
        givenMatches(
            resolveInfo(packageName = "com.example.first", label = "First"),
            resolveInfo(packageName = "com.example.system", label = "System", isSystem = true),
        )

        assertEquals("System", repository.resolveLabel(INTENT))
    }

    @Test
    fun resolveLabel_withoutAKnownOrSystemPackage_returnsTheFirstLabel() {
        givenMatches(
            resolveInfo(packageName = "com.example.first", label = "First"),
            resolveInfo(packageName = "com.example.second", label = "Second"),
        )

        assertEquals("First", repository.resolveLabel(INTENT))
    }

    private fun givenResolvedActivity(resolveInfo: ResolveInfo?) {
        every { packageManager.resolveActivity(any(), any<Int>()) } returns resolveInfo
    }

    private fun givenMatches(vararg matches: ResolveInfo) {
        every {
            packageManager.queryIntentActivities(any(), any<Int>())
        } returns matches.toList()
    }

    private fun resolveInfo(
        packageName: String,
        label: String,
        isSystem: Boolean = false,
        isConcreteMatch: Boolean = false,
    ): ResolveInfo {
        return ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                applicationInfo = ApplicationInfo().apply {
                    this.packageName = packageName
                    flags = when {
                        isSystem -> ApplicationInfo.FLAG_SYSTEM
                        else -> 0
                    }
                }
            }
            nonLocalizedLabel = label
            match = when {
                isConcreteMatch -> IntentFilter.MATCH_CATEGORY_TYPE
                else -> 0
            }
        }
    }

    private companion object {
        val INTENT: Intent = Intent(Intent.ACTION_VIEW)
    }
}
