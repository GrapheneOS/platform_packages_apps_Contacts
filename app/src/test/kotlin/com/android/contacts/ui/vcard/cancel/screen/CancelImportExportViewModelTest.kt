package com.android.contacts.ui.vcard.cancel.screen

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.android.contacts.domain.vcard.usecase.CancelVCardImportExport
import com.android.contacts.tests.MainDispatcherRule
import com.android.contacts.ui.vcard.cancel.CancelImportExportActivity
import com.android.contacts.ui.vcard.cancel.screen.model.CancelImportExportAction as Action
import com.android.contacts.ui.vcard.cancel.screen.model.CancelImportExportEffect as Effect
import com.android.contacts.ui.vcard.cancel.screen.model.CancelImportExportUiState as State
import com.android.contacts.ui.vcard.cancel.screen.model.VCardOperationType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
internal class CancelImportExportViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val cancelVCardImportExport = mockk<CancelVCardImportExport>()

    @Test(expected = IllegalArgumentException::class)
    fun whenDataIsMissing_throwsIllegalArgumentException() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            createViewModel(
                initialState = mapOf(),
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun whenDataParamsAreMissing_throwsIllegalArgumentException() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            createViewModel(
                initialState = mapOf(CancelImportExportViewModel.EXTRA_DATA to "".toUri()),
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun whenJobIdIsInvalid_throwsIllegalArgumentException() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            createViewModel(
                initialState = mapOf(
                    CancelImportExportViewModel.EXTRA_DATA to buildUri(jobId = "invalid"),
                ),
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun whenOperationTypeIsInvalid_throwsIllegalArgumentException() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            createViewModel(
                initialState = mapOf(
                    CancelImportExportViewModel.EXTRA_DATA to buildUri(type = "invalid_type"),
                ),
            )
        }
    }

    @Test
    fun whenConfirmImport_onConfirm_cancelImportSuccessfully() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            coEvery { cancelVCardImportExport(any(), any()) } returns true

            val viewModel = createViewModel(
                initialState = mapOf(
                    CancelImportExportViewModel.EXTRA_DATA to
                        CancelImportExportActivity.buildUri(
                            1,
                            "contacts.vcf",
                            VCardOperationType.IMPORT,
                        ),
                ),
            )

            viewModel.uiState.test {
                assertEquals(
                    State.Confirming(
                        type = VCardOperationType.IMPORT,
                        displayName = "contacts.vcf",
                    ),
                    awaitItem(),
                )
                viewModel.onAction(Action.CONFIRM)
                advanceUntilIdle()
            }

            viewModel.effects.test {
                assertEquals(Effect.CLOSE, awaitItem())
            }

            coVerify { cancelVCardImportExport(1, "contacts.vcf") }
        }
    }

    @Test
    fun whenConfirmExport_onConfirm_cancelExportSuccessfully() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            coEvery { cancelVCardImportExport(any(), any()) } returns true

            val viewModel = createViewModel(
                initialState = mapOf(
                    CancelImportExportViewModel.EXTRA_DATA to
                        CancelImportExportActivity.buildUri(
                            1,
                            "contacts.vcf",
                            VCardOperationType.EXPORT,
                        ),
                ),
            )

            viewModel.uiState.test {
                assertEquals(
                    State.Confirming(
                        type = VCardOperationType.EXPORT,
                        displayName = "contacts.vcf",
                    ),
                    awaitItem(),
                )
                viewModel.onAction(Action.CONFIRM)
                advanceUntilIdle()
            }

            viewModel.effects.test {
                assertEquals(Effect.CLOSE, awaitItem())
            }

            coVerify { cancelVCardImportExport(1, "contacts.vcf") }
        }
    }

    @Test
    fun onCancelError_showErrorDialog() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            coEvery { cancelVCardImportExport(any(), any()) } returns false

            val viewModel = createViewModel(
                initialState = mapOf(CancelImportExportViewModel.EXTRA_DATA to URI),
            )

            viewModel.uiState.test {
                viewModel.onAction(Action.CONFIRM)
                advanceUntilIdle()
                assertEquals(State.Error, expectMostRecentItem())
            }

            coVerify { cancelVCardImportExport(1, "contacts.vcf") }

            viewModel.effects.test {
                expectNoEvents()
            }

            viewModel.onAction(Action.DISMISS)
            advanceUntilIdle()

            viewModel.effects.test {
                assertEquals(Effect.CLOSE, awaitItem())
            }
        }
    }

    private fun createViewModel(
        initialState: Map<String, Any> = mapOf(
            CancelImportExportViewModel.EXTRA_DATA to URI,
        ),
    ): CancelImportExportViewModel = CancelImportExportViewModel(
        savedStateHandle = SavedStateHandle(initialState),
        cancelVCardImportExport = cancelVCardImportExport,
    )

    private fun buildUri(
        jobId: String? = "1",
        displayName: String? = "",
        type: String? = VCardOperationType.IMPORT.value,
    ): Uri {
        return URI.buildUpon()
            .clearQuery()
            .appendQueryParameter(
                CancelImportExportViewModel.QUERY_PARAM_JOB_ID,
                jobId,
            )
            .appendQueryParameter(
                CancelImportExportViewModel.QUERY_PARAM_DISPLAY_NAME,
                displayName,
            )
            .appendQueryParameter(
                CancelImportExportViewModel.QUERY_PARAM_TYPE,
                type,
            ).build()
    }

    private companion object {
        val URI = CancelImportExportActivity.buildUri(1, "contacts.vcf", VCardOperationType.IMPORT)
    }
}
