package com.android.contacts.ui.vcardimport

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.android.contacts.domain.util.IsPermissionGranted
import com.android.contacts.domain.vcard.model.ImportVCardError as Error
import com.android.contacts.domain.vcard.usecase.BuildVCardSource
import com.android.contacts.domain.vcard.usecase.ImportVCards
import com.android.contacts.tests.MainDispatcherRule
import com.android.contacts.tests.factory.AccountModelFactory
import com.android.contacts.tests.factory.ImportVCardSourceFactory
import com.android.contacts.ui.vcardimport.screen.ImportVCardViewModel
import com.android.contacts.ui.vcardimport.screen.model.ImportVCardAction as Action
import com.android.contacts.ui.vcardimport.screen.model.ImportVCardEffect as Effect
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ImportVCardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun onResume_whenPermissionsAreNotGranted_requestThem() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(
                isPermissionGranted = { false },
            )

            viewModel.effects.test {
                viewModel.onResume()
                advanceUntilIdle()

                assertEquals(
                    Effect.RequestPermissions(ImportVCardViewModel.PERMISSIONS_REQUIRED),
                    awaitItem(),
                )
            }
        }

    @Test
    fun onResume_whenPermissionsAreGranted_selectFiles() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(
                isPermissionGranted = { true },
            )

            viewModel.effects.test {
                viewModel.onResume()
                advanceUntilIdle()
                assertEquals(Effect.SelectFiles, awaitItem())
            }
        }

    @Test
    fun onPermissionsGranted_whenThereIsAnInitialFile_buildSourceAndSelectAccount() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val source = ImportVCardSourceFactory.build()
            val buildVCardSource = mockk<BuildVCardSource> {
                coEvery { this@mockk.invoke(any()) } returns source
            }
            val savedStateHandle = SavedStateHandle(
                mapOf(ImportVCardViewModel.KEY_INITIAL_FILE to source.uri),
            )
            val viewModel = createViewModel(
                savedStateHandle = savedStateHandle,
                isPermissionGranted = { true },
                buildVCardSource = buildVCardSource,
            )

            viewModel.effects.test {
                viewModel.onResume()
                advanceUntilIdle()
                assertEquals(Effect.SelectAccount, awaitItem())
            }

            coVerify { buildVCardSource(source.uri) }
            assertEquals(listOf(source), savedStateHandle[ImportVCardViewModel.KEY_SOURCES])
        }

    @Test
    fun onFilesSelected_whenListIsEmpty_close() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.effects.test {
                viewModel.onResume()
                advanceUntilIdle()
                assertEquals(Effect.SelectFiles, awaitItem())
                viewModel.onAction(Action.FilesSelected(emptyList()))
                advanceUntilIdle()
                assertEquals(Effect.Close, awaitItem())
            }
        }

    @Test
    fun onFilesSelected_whenListIsNotEmpty_close() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val source = ImportVCardSourceFactory.build()
            val buildVCardSource = mockk<BuildVCardSource> {
                coEvery { this@mockk.invoke(any()) } returns source
            }
            val savedStateHandle = SavedStateHandle()
            val viewModel = createViewModel(
                savedStateHandle = savedStateHandle,
                buildVCardSource = buildVCardSource,
            )

            viewModel.effects.test {
                viewModel.onResume()
                advanceUntilIdle()
                assertEquals(Effect.SelectFiles, awaitItem())
                viewModel.onAction(Action.FilesSelected(listOf(source.uri)))
                advanceUntilIdle()
                assertEquals(Effect.SelectAccount, awaitItem())
            }

            coVerify { buildVCardSource(source.uri) }
            assertEquals(listOf(source), savedStateHandle[ImportVCardViewModel.KEY_SOURCES])
        }

    @Test
    fun onSelectAccount_whenAccountIsNull_close() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val source = ImportVCardSourceFactory.build()
            val viewModel = createViewModel(
                savedStateHandle = SavedStateHandle(
                    mapOf(ImportVCardViewModel.KEY_SOURCES to listOf(source)),
                ),
            )

            viewModel.effects.test {
                viewModel.onResume()
                advanceUntilIdle()
                assertEquals(Effect.SelectAccount, awaitItem())
                viewModel.onAction(Action.AccountSelected(null))
                advanceUntilIdle()
                assertEquals(Effect.Close, awaitItem())
            }
        }

    @Test
    fun onSelectAccount_whenAccountIsNotNull_startImport() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val source = ImportVCardSourceFactory.build()
            val account = AccountModelFactory.build()
            val importVCards = mockk<ImportVCards> {
                every { this@mockk.invoke(any(), any()) } returns emptyFlow()
            }
            val viewModel = createViewModel(
                savedStateHandle = SavedStateHandle(
                    mapOf(
                        ImportVCardViewModel.KEY_SOURCES to listOf(source),
                        ImportVCardViewModel.KEY_ACCOUNT to account,
                    ),
                ),
                importVCards = importVCards,
            )

            viewModel.effects.test {
                viewModel.onResume()
                advanceUntilIdle()
                assertEquals(Effect.Close, awaitItem())
            }

            verify { importVCards(account, listOf(source)) }
        }

    @Test
    fun onImport_whenErrorIsEmitted_showIt() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val error = Error.OutOfMemory
            val importVCards = mockk<ImportVCards> {
                every { this@mockk.invoke(any(), any()) } returns flowOf(error)
            }
            val viewModel = createViewModel(
                savedStateHandle = SavedStateHandle(
                    mapOf(
                        ImportVCardViewModel.KEY_SOURCES to listOf(
                            ImportVCardSourceFactory.build()
                        ),
                        ImportVCardViewModel.KEY_ACCOUNT to AccountModelFactory.build(),
                    ),
                ),
                importVCards = importVCards,
            )

            viewModel.effects.test {
                viewModel.onResume()
                advanceUntilIdle()
                assertEquals(Effect.ShowImportError(error), awaitItem())
                assertEquals(Effect.Close, awaitItem())
            }
        }

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        isPermissionGranted: IsPermissionGranted = { true },
        buildVCardSource: BuildVCardSource = { null },
        importVCards: ImportVCards = { _, _ -> emptyFlow() },
    ): ImportVCardViewModel = ImportVCardViewModel(
        savedStateHandle = savedStateHandle,
        isPermissionGranted = isPermissionGranted,
        buildVCardSource = buildVCardSource,
        importVCards = importVCards,
    )
}
