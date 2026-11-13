package com.android.contacts.ui.interactions.attachphoto.screen

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Settings
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.core.net.toUri
import com.android.contacts.ui.interactions.attachphoto.screen.model.AttachPhotoAction as Action
import com.android.contacts.ui.interactions.attachphoto.screen.model.AttachPhotoEffect as Effect
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class AttachPhotoEffectHandlerImplTest {

    private val activity = mockk<Activity>(relaxed = true)
    private val packageManager = mockk<PackageManager>()
    private val pickContactLauncher = mockk<ActivityResultLauncher<Intent>>(relaxed = true)
    private val cropPhotoLauncher = mockk<ActivityResultLauncher<Intent>>(relaxed = true)
    private val pickDefaultAccountLauncher = mockk<ActivityResultLauncher<Intent>>(relaxed = true)
    private val actions = mutableListOf<Action>()

    private val effectHandler: AttachPhotoEffectHandler = AttachPhotoEffectHandlerImpl(
        activity = activity,
        pickContactLauncher = pickContactLauncher,
        cropPhotoLauncher = cropPhotoLauncher,
        pickDefaultAccountLauncher = pickDefaultAccountLauncher,
        onAction = actions::add,
    )

    @Before
    fun setUp() {
        every { activity.packageName } returns PACKAGE_NAME
        every { activity.packageManager } returns packageManager
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun pickContact_launchesInAppContactPicker() {
        val intent = slot<Intent>()

        effectHandler.handle(Effect.PickContact)

        verify { pickContactLauncher.launch(capture(intent)) }
        assertEquals(Intent.ACTION_PICK, intent.captured.action)
        assertEquals(Contacts.CONTENT_TYPE, intent.captured.type)
        assertEquals(PACKAGE_NAME, intent.captured.`package`)
    }

    @Test
    fun cropPhoto_whenCropperExists_launchesItWithCropExtras() {
        givenCropHandler(CROP_PACKAGE_NAME)
        val intent = slot<Intent>()

        effectHandler.handle(CROP_EFFECT)

        verify { cropPhotoLauncher.launch(capture(intent)) }
        with(intent.captured) {
            assertEquals("com.android.camera.action.CROP", action)
            assertEquals(INPUT_URI, data)
            assertEquals(MIME_TYPE, type)
            assertEquals(CROP_PACKAGE_NAME, `package`)
            assertEquals(OUTPUT_URI, getParcelableExtra(MediaStore.EXTRA_OUTPUT, Uri::class.java))
            assertEquals(CROP_SIZE, getIntExtra("outputX", 0))
            assertEquals(CROP_SIZE, getIntExtra("outputY", 0))
            assertTrue(flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION != 0)
        }
        assertTrue(actions.isEmpty())
    }

    @Test
    fun cropPhoto_whenMimeTypeIsNull_launchesWithDataOnly() {
        givenCropHandler(CROP_PACKAGE_NAME)
        val intent = slot<Intent>()

        effectHandler.handle(CROP_EFFECT.copy(mimeType = null))

        verify { cropPhotoLauncher.launch(capture(intent)) }
        assertEquals(INPUT_URI, intent.captured.data)
        assertEquals(null, intent.captured.type)
    }

    @Test
    fun cropPhoto_whenNoCropperExists_reportsCropUnavailable() {
        every { packageManager.queryIntentActivities(any<Intent>(), any<Int>()) } returns
            emptyList()

        effectHandler.handle(CROP_EFFECT)

        assertEquals(listOf<Action>(Action.CropUnavailable), actions)
        verify(exactly = 0) { cropPhotoLauncher.launch(any()) }
    }

    @Test
    fun cropPhoto_whenLaunchFails_fallsBackToCropUnavailable() {
        givenCropHandler(CROP_PACKAGE_NAME)
        every { cropPhotoLauncher.launch(any()) } throws ActivityNotFoundException()

        effectHandler.handle(CROP_EFFECT)

        assertEquals(listOf<Action>(Action.CropUnavailable), actions)
        verify(exactly = 0) { activity.finish() }
    }

    @Test
    fun pickDefaultAccount_launchesDefaultAccountSettings() {
        val intent = slot<Intent>()

        effectHandler.handle(Effect.PickDefaultAccount)

        verify { pickDefaultAccountLauncher.launch(capture(intent)) }
        assertEquals(Settings.ACTION_SET_DEFAULT_ACCOUNT, intent.captured.action)
        assertTrue(intent.captured.flags and Intent.FLAG_ACTIVITY_CLEAR_TOP != 0)
        assertTrue(intent.captured.flags and Intent.FLAG_ACTIVITY_SINGLE_TOP != 0)
    }

    @Test
    fun close_whenSuccess_setsResultOkAndFinishes() {
        effectHandler.handle(Effect.Close(isSuccess = true))

        verifyOrder {
            activity.setResult(Activity.RESULT_OK)
            activity.finish()
        }
    }

    @Test
    fun close_whenFailure_setsResultCanceledAndFinishes() {
        effectHandler.handle(Effect.Close(isSuccess = false))

        verifyOrder {
            activity.setResult(Activity.RESULT_CANCELED)
            activity.finish()
        }
    }

    private fun givenCropHandler(packageName: String) {
        val resolveInfo = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply { this.packageName = packageName }
        }
        every {
            packageManager.queryIntentActivities(
                any<Intent>(),
                PackageManager.MATCH_DEFAULT_ONLY or PackageManager.MATCH_SYSTEM_ONLY,
            )
        } returns listOf(resolveInfo)
    }

    private companion object {
        const val PACKAGE_NAME = "com.android.contacts"
        const val CROP_PACKAGE_NAME = "com.example.gallery"
        const val MIME_TYPE = "image/jpeg"
        const val CROP_SIZE = 720
        val INPUT_URI = "content://com.android.contacts.files/temp.jpg".toUri()
        val OUTPUT_URI = "content://com.android.contacts.files/cropped.jpg".toUri()
        val CROP_EFFECT = Effect.CropPhoto(
            inputUri = INPUT_URI,
            outputUri = OUTPUT_URI,
            mimeType = MIME_TYPE,
            size = CROP_SIZE,
        )
    }
}
