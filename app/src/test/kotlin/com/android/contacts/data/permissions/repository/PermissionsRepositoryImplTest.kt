package com.android.contacts.data.permissions.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class PermissionsRepositoryImplTest {

    private val context = mockk<Context>()

    private val repository = PermissionsRepositoryImpl(context = context)

    @Test
    fun isCallLogGranted_whenPermissionIsGranted_isTrue() = runTest {
        givenCallLogPermission(PackageManager.PERMISSION_GRANTED)

        assertTrue(repository.isCallLogGranted())
    }

    @Test
    fun isCallLogGranted_whenPermissionIsDenied_isFalse() = runTest {
        givenCallLogPermission(PackageManager.PERMISSION_DENIED)

        assertFalse(repository.isCallLogGranted())
    }

    private fun givenCallLogPermission(result: Int) {
        every { context.checkSelfPermission(Manifest.permission.READ_CALL_LOG) } returns result
    }
}
