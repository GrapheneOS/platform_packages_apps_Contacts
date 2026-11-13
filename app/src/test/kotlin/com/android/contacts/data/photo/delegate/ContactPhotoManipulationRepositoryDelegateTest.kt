package com.android.contacts.data.photo.delegate

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.ExifInterface
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.android.contacts.ContactsUtils
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
// So ImageDecoder works correctly
@GraphicsMode(GraphicsMode.Mode.NATIVE)
internal class ContactPhotoManipulationRepositoryDelegateTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private val subject: ContactPhotoManipulationRepositoryDelegate =
        ContactPhotoManipulationRepositoryDelegateImpl(
            context = context,
            contentResolver = context.contentResolver,
            ioDispatcher = UnconfinedTestDispatcher(),
        )

    @Before
    fun setUp() {
        mockkStatic(ContactsUtils::class)
        every { ContactsUtils.getThumbnailSize(any()) } returns THUMBNAIL_SIZE
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun cropToSquare_landscape_writesCenteredSquareAtRequestedSize() = runTest {
        val source = givenJpeg(width = 400, height = 200)
        val output = newFileUri()

        assertTrue(subject.cropToSquare(source, output, size = 100))

        val result = decode(output)
        assertEquals(100, result.width)
        assertEquals(100, result.height)
        // The source is red on the left half and blue on the right half; the center crop keeps
        // both halves side by side
        assertColorClose(Color.RED, result.getPixel(10, 50))
        assertColorClose(Color.BLUE, result.getPixel(90, 50))
    }

    @Test
    fun cropToSquare_smallerThanSize_doesNotUpscale() = runTest {
        val source = givenJpeg(width = 80, height = 60)
        val output = newFileUri()

        assertTrue(subject.cropToSquare(source, output, size = 720))

        val result = decode(output)
        assertEquals(60, result.width)
        assertEquals(60, result.height)
    }

    @Test
    fun cropToSquare_appliesExifOrientation() = runTest {
        // Stored as landscape red|blue, but EXIF says to rotate it 90° clockwise, which turns it
        // into portrait with red on top and blue on the bottom
        val source = givenJpeg(
            width = 400,
            height = 200,
            orientation = ExifInterface.ORIENTATION_ROTATE_90,
        )
        val output = newFileUri()

        assertTrue(subject.cropToSquare(source, output, size = 100))

        val result = decode(output)
        assertColorClose(Color.RED, result.getPixel(50, 10))
        assertColorClose(Color.BLUE, result.getPixel(50, 90))
    }

    @Test
    fun cropToSquare_withTransparency_writesJpeg() = runTest {
        val source = givenPng(width = 200, height = 100)
        val output = newFileUri()

        assertTrue(subject.cropToSquare(source, output, size = 50))

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(output.path, options)
        assertEquals("image/jpeg", options.outMimeType)
        assertEquals(50, options.outWidth)
        assertEquals(50, options.outHeight)
    }

    @Test
    fun cropToSquare_whenSourceIsMissing_returnsFalse() = runTest {
        val output = newFileUri()

        assertFalse(subject.cropToSquare(missingFileUri(), output, size = 100))
    }

    @Test
    fun cropToSquare_whenSourceIsNotAnImage_returnsFalse() = runTest {
        val source = temporaryFolder.newFile("not-an-image.jpg")
            .apply { writeText("not an image") }
            .let(Uri::fromFile)

        assertFalse(subject.cropToSquare(source, newFileUri(), size = 100))
    }

    @Test
    fun buildThumbnail_returnsSquarePngAtThumbnailSize() = runTest {
        val source = givenJpeg(width = 400, height = 300)

        val thumbnail = subject.buildThumbnail(source)

        assertNotNull(thumbnail)
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail!!.size, options)
        assertEquals("image/png", options.outMimeType)
        assertEquals(THUMBNAIL_SIZE, options.outWidth)
        assertEquals(THUMBNAIL_SIZE, options.outHeight)
    }

    @Test
    fun buildThumbnail_smallerThanThumbnailSize_upscales() = runTest {
        val source = givenJpeg(width = 60, height = 60)

        val thumbnail = subject.buildThumbnail(source)

        assertNotNull(thumbnail)
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail!!.size, options)
        assertEquals(THUMBNAIL_SIZE, options.outWidth)
        assertEquals(THUMBNAIL_SIZE, options.outHeight)
    }

    @Test
    fun buildThumbnail_whenSourceIsMissing_returnsNull() = runTest {
        assertNull(subject.buildThumbnail(missingFileUri()))
    }

    /** Writes a JPEG that is red on its left half and blue on its right half. */
    private fun givenJpeg(
        width: Int,
        height: Int,
        orientation: Int = ExifInterface.ORIENTATION_NORMAL,
    ): Uri {
        val file = temporaryFolder.newFile()
        file.outputStream().use { out ->
            halvesBitmap(width, height).compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        if (orientation != ExifInterface.ORIENTATION_NORMAL) {
            ExifInterface(file.path).apply {
                setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
                saveAttributes()
            }
        }
        return Uri.fromFile(file)
    }

    private fun givenPng(width: Int, height: Int): Uri {
        val file = temporaryFolder.newFile()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            .apply { eraseColor(Color.TRANSPARENT) }
        file.outputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
        return Uri.fromFile(file)
    }

    private fun halvesBitmap(width: Int, height: Int): Bitmap {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            val canvas = Canvas(this)
            val half = width / 2f
            val red = Paint().apply { color = Color.RED }
            val blue = Paint().apply { color = Color.BLUE }
            canvas.drawRect(0f, 0f, half, height.toFloat(), red)
            canvas.drawRect(half, 0f, width.toFloat(), height.toFloat(), blue)
        }
    }

    private fun newFileUri(): Uri = Uri.fromFile(temporaryFolder.newFile())

    private fun missingFileUri(): Uri = Uri.fromFile(File(temporaryFolder.root, "missing.jpg"))

    private fun decode(uri: Uri): Bitmap = BitmapFactory.decodeFile(uri.path)

    private fun assertColorClose(expected: Int, actual: Int) {
        val tolerance = 40
        val matches = listOf(Color::red, Color::green, Color::blue).all { channel ->
            kotlin.math.abs(channel(expected) - channel(actual)) <= tolerance
        }
        assertTrue(
            "Expected ${Integer.toHexString(expected)} but was ${Integer.toHexString(actual)}",
            matches,
        )
    }

    private companion object {
        const val THUMBNAIL_SIZE = 96
    }
}
