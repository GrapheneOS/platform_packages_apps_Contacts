package com.android.contacts.data.appinfo.repository

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AppInfoRepositoryImplTest {

    private val context = mockk<Context>()
    private val packageManager = mockk<PackageManager>()

    private val repository = AppInfoRepositoryImpl(
        context = context,
        packageManager = packageManager,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @Before
    fun setUp() {
        every { context.packageName } returns PACKAGE_NAME
    }

    @Test
    fun getBuildVersion_returnsTheVersionOfTheInstalledPackage() = runTest {
        val packageInfo = PackageInfo().apply { versionName = "1.7.40" }
        givenPackageInfo { packageInfo }

        assertEquals("1.7.40", repository.getBuildVersion())
    }

    @Test
    fun getBuildVersion_whenPackageHasNoVersion_returnsNull() = runTest {
        givenPackageInfo { PackageInfo() }

        assertNull(repository.getBuildVersion())
    }

    @Test
    fun getBuildVersion_whenPackageIsNotFound_returnsNull() = runTest {
        givenPackageInfo { throw PackageManager.NameNotFoundException() }

        assertNull(repository.getBuildVersion())
    }

    private fun givenPackageInfo(packageInfo: () -> PackageInfo) {
        every {
            packageManager.getPackageInfo(
                PACKAGE_NAME,
                any<PackageManager.PackageInfoFlags>(),
            )
        } answers { packageInfo() }
    }

    private companion object {
        const val PACKAGE_NAME = "com.android.contacts"
    }
}
