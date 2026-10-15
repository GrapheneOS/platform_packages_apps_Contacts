package com.android.contacts.ui.vcard.export.screen

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.android.contacts.domain.vcard.model.ExportConfig
import com.android.contacts.domain.vcard.usecase.CreateTempExportFile
import com.android.contacts.domain.vcard.usecase.ExportVCard
import com.android.contacts.domain.vcard.usecase.GetExportConfig
import com.android.contacts.domain.vcard.usecase.ResolveFileDisplayName
import com.android.contacts.tests.MainDispatcherRule
import com.android.contacts.ui.vcard.export.screen.ExportVCardViewModel.Step
import com.android.contacts.ui.vcard.export.screen.model.ExportMode
import com.android.contacts.ui.vcard.export.screen.model.ExportVCardAction as Action
import com.android.contacts.ui.vcard.export.screen.model.ExportVCardEffect as Effect
import com.android.contacts.util.core.IsPermissionGranted
import com.android.contacts.vcard.ExportRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.io.File
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
internal class ExportVCardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val exportVCard = mockk<ExportVCard>()
    private val exportRequestSlot = slot<ExportRequest>()

    @Before
    fun setUp() {
        every { exportVCard(capture(exportRequestSlot)) } returns emptyFlow()
    }

    @Test
    fun close_whenNoModesAvailable() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(
                getExportConfig = {
                    ExportConfig(
                        canExportContacts = false,
                        canShareContacts = false,
                    )
                },
            )

            viewModel.effects.test {
                viewModel.onResume()
                advanceUntilIdle()
                assertEquals(
                    "Dialog did not close when no modes are available",
                    Effect.Close,
                    awaitItem(),
                )
            }
        }

    @Test
    fun setAvailableModesAccordingToConfig() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(
                getExportConfig = {
                    ExportConfig(
                        canExportContacts = false,
                        canShareContacts = true,
                    )
                },
            )
            viewModel.onResume()
            advanceUntilIdle()

            assertEquals(
                "Wrong available modes for given config",
                persistentSetOf(ExportMode.SHARE_ALL),
                viewModel.uiState.value.availableModes,
            )
        }

    @Test
    fun requestPermissions_whenPermissionsAreNotGranted() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(
                isPermissionGranted = { false },
            )

            viewModel.effects.test {
                viewModel.onResume()
                advanceUntilIdle()
                assertEquals(
                    "Permissions should have been requested",
                    Effect.RequestPermissions(ExportVCardViewModel.PERMISSIONS_REQUIRED),
                    awaitItem(),
                )
            }
        }

    @Test
    fun close_whenPermissionsAreRequestedAndNotGranted() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(
                isPermissionGranted = { false },
            )

            viewModel.effects.test {
                viewModel.onResume()
                advanceUntilIdle()
                assertEquals(
                    "Permissions should have been requested",
                    Effect.RequestPermissions(ExportVCardViewModel.PERMISSIONS_REQUIRED),
                    awaitItem(),
                )

                viewModel.onAction(Action.PermissionRequestFinished)
                advanceUntilIdle()
                assertEquals(
                    "Dialog should have closed if the permission request finished" +
                        " and the permissions were not granted",
                    Effect.Close,
                    awaitItem(),
                )
            }
        }

    @Test
    fun showDialog_whenPermissionsAreGranted() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(
                isPermissionGranted = { true },
            )

            viewModel.onResume()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.showModeDialog)
        }

    @Test
    fun whenShareModeIsSelected_startExportWithTempFile() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val filename = "test.vcf"
            val fileUri = Uri.fromFile(File(filename))
            val viewModel = createViewModel(
                isPermissionGranted = { true },
                createTempExportFile = { fileUri },
                resolveFileDisplayName = { filename },
            )

            viewModel.onResume()
            advanceUntilIdle()
            viewModel.onAction(Action.ModeSelected(ExportMode.SHARE_ALL))
            advanceUntilIdle()

            verify { exportVCard(any()) }

            val request = exportRequestSlot.captured
            assertEquals(fileUri, request.destUri)
            assertEquals(filename, request.displayName)
        }

    @Test
    fun whenShareModeIsSelectedMultipleTimes_onlyStartOneExport() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val filename = "test.vcf"
            val fileUri = Uri.fromFile(File(filename))
            val viewModel = createViewModel(
                isPermissionGranted = { true },
                createTempExportFile = { fileUri },
                resolveFileDisplayName = { filename },
            )

            viewModel.onResume()
            advanceUntilIdle()
            repeat(3) {
                viewModel.onAction(Action.ModeSelected(ExportMode.SHARE_ALL))
            }
            advanceUntilIdle()

            verify(exactly = 1) { exportVCard(any()) }
        }

    @Test
    fun whenVCardFileModeIsSelected_openSelectFile() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(
                isPermissionGranted = { true },
            )

            viewModel.effects.test {
                viewModel.onResume()
                advanceUntilIdle()
                viewModel.onAction(Action.ModeSelected(ExportMode.VCARD_FILE))
                advanceUntilIdle()

                assertEquals(
                    "Select file should have been requested",
                    Effect.SelectFile,
                    awaitItem(),
                )
            }
        }

    @Test
    fun whenVCardFileIsSelected_startExport() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val filename = "test.vcf"
            val fileUri = Uri.fromFile(File(filename))
            val viewModel = createViewModel(
                isPermissionGranted = { true },
                resolveFileDisplayName = { filename },
            )

            viewModel.onResume()
            advanceUntilIdle()
            viewModel.onAction(Action.ModeSelected(ExportMode.VCARD_FILE))
            advanceUntilIdle()
            viewModel.onAction(Action.FileSelected(fileUri))

            verify { exportVCard(any()) }

            val request = exportRequestSlot.captured
            assertEquals(fileUri, request.destUri)
            assertEquals(filename, request.displayName)
        }

    @Test
    fun whenExportFails_showErrorAndClose() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val filename = "test.vcf"
            val fileUri = Uri.fromFile(File(filename))
            givenExportResponse(false)
            val viewModel = createViewModel(
                isPermissionGranted = { true },
                createTempExportFile = { fileUri },
                resolveFileDisplayName = { filename },
            )

            viewModel.effects.test {
                viewModel.onResume()
                advanceUntilIdle()
                viewModel.onAction(Action.ModeSelected(ExportMode.SHARE_ALL))
                advanceUntilIdle()

                assertEquals(Effect.ShowError, awaitItem())
                assertEquals(Effect.Close, awaitItem())
            }
        }

    @Test
    fun whenExportSucceeds_close() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val filename = "test.vcf"
            val fileUri = Uri.fromFile(File(filename))
            givenExportResponse(true)
            val viewModel = createViewModel(
                isPermissionGranted = { true },
                createTempExportFile = { fileUri },
                resolveFileDisplayName = { filename },
            )

            viewModel.effects.test {
                viewModel.onResume()
                advanceUntilIdle()
                viewModel.onAction(Action.ModeSelected(ExportMode.SHARE_ALL))
                advanceUntilIdle()

                assertEquals(Effect.Close, awaitItem())
            }
        }

    @Test
    fun whenRequestingPermissions_onProcessRestartFollowingResponse_moveToSelectingMode() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val savedStateHandle = SavedStateHandle(
                mapOf(ExportVCardViewModel.KEY_STEP to Step.REQUESTING_PERMISSIONS),
            )
            val viewModel = createViewModel(
                savedStateHandle = savedStateHandle,
                isPermissionGranted = { true },
            )

            viewModel.uiState.test {
                viewModel.onAction(Action.PermissionRequestFinished)
                viewModel.onResume()
                advanceUntilIdle()

                assertTrue(expectMostRecentItem().showModeDialog)
                assertEquals(
                    Step.SELECTING_MODE,
                    savedStateHandle[ExportVCardViewModel.KEY_STEP],
                )
            }
        }

    @Test
    fun whenSelectingMode_onProcessRestart_isStillSelectingMode() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val savedStateHandle = SavedStateHandle(
                mapOf(ExportVCardViewModel.KEY_STEP to Step.SELECTING_MODE),
            )
            val viewModel = createViewModel(
                savedStateHandle = savedStateHandle,
                isPermissionGranted = { true },
            )

            viewModel.uiState.test {
                viewModel.onResume()
                advanceUntilIdle()

                assertTrue(expectMostRecentItem().showModeDialog)
                assertEquals(
                    Step.SELECTING_MODE,
                    savedStateHandle[ExportVCardViewModel.KEY_STEP],
                )
            }
        }

    @Test
    fun whenSelectingFile_onProcessRestartAndResponse_startsExport() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val savedStateHandle = SavedStateHandle(
                mapOf(ExportVCardViewModel.KEY_STEP to Step.SELECTING_FILE),
            )
            val filename = "test.vcf"
            val fileUri = Uri.fromFile(File(filename))
            val viewModel = createViewModel(
                savedStateHandle = savedStateHandle,
                isPermissionGranted = { true },
                resolveFileDisplayName = { filename },
            )

            viewModel.onAction(Action.FileSelected(fileUri))
            viewModel.onResume()
            advanceUntilIdle()

            verify { exportVCard(any()) }

            val request = exportRequestSlot.captured
            assertEquals(fileUri, request.destUri)
            assertEquals(filename, request.displayName)
        }

    @Test
    fun whenSelectingMode_onMultipleResumes_onlyTriggerOneEffectEach() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val savedStateHandle = SavedStateHandle(
                mapOf(ExportVCardViewModel.KEY_STEP to Step.SELECTING_MODE),
            )
            val viewModel = createViewModel(
                savedStateHandle = savedStateHandle,
                isPermissionGranted = { true },
            )

            viewModel.effects.test {
                repeat(3) {
                    viewModel.onResume()
                    advanceUntilIdle()
                }
                viewModel.onAction(Action.ModeSelected(ExportMode.VCARD_FILE))
                assertEquals(Effect.SelectFile, awaitItem())
            }
        }

    @Test
    fun whenFinishing_close() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(
                savedStateHandle = SavedStateHandle(
                    mapOf(ExportVCardViewModel.KEY_STEP to Step.FINISHING),
                ),
            )

            viewModel.effects.test {
                viewModel.onResume()
                advanceUntilIdle()
                assertEquals(Effect.Close, awaitItem())
            }
        }

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        getExportConfig: GetExportConfig = {
            ExportConfig(
                canExportContacts = true,
                canShareContacts = true,
            )
        },
        isPermissionGranted: IsPermissionGranted = { true },
        createTempExportFile: CreateTempExportFile = { null },
        resolveFileDisplayName: ResolveFileDisplayName = { "" },
    ): ExportVCardViewModel = ExportVCardViewModel(
        savedStateHandle = savedStateHandle,
        getExportConfig = getExportConfig,
        isPermissionGranted = isPermissionGranted,
        createTempExportFile = createTempExportFile,
        resolveFileDisplayName = resolveFileDisplayName,
        exportVCard = exportVCard,
    )

    private fun givenExportResponse(response: Boolean) {
        every { exportVCard(capture(exportRequestSlot)) } returns flowOf(response)
    }
}
