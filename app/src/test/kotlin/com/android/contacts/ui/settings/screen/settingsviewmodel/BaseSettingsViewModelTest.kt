package com.android.contacts.ui.settings.screen.settingsviewmodel

import com.android.contacts.data.profile.model.ProfileData
import com.android.contacts.data.profile.repository.ProfileRepository
import com.android.contacts.data.settings.model.SortOrder
import com.android.contacts.data.settings.repository.DisplaySettingsRepository
import com.android.contacts.data.simimport.model.SimImportResult
import com.android.contacts.data.simimport.repository.SimImportResultRepository
import com.android.contacts.domain.settings.model.SettingsData
import com.android.contacts.domain.settings.usecase.GetSettingsData
import com.android.contacts.tests.MainDispatcherRule
import com.android.contacts.ui.settings.screen.SettingsViewModel
import com.android.contacts.ui.settings.screen.mapper.SettingsUiStateMapper
import com.android.contacts.ui.settings.screen.model.SettingsUiState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule

@OptIn(ExperimentalCoroutinesApi::class)
internal abstract class BaseSettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    protected val getSettingsData = mockk<GetSettingsData>()
    protected val displaySettingsRepository = mockk<DisplaySettingsRepository>(relaxed = true)
    protected val settingsUiStateMapper = mockk<SettingsUiStateMapper>()

    protected val profiles = MutableStateFlow(ProfileData())
    protected val simImportResults = MutableSharedFlow<SimImportResult>(extraBufferCapacity = 1)

    protected val settingsData = mockk<SettingsData>()
    protected val reloadedSettingsData = mockk<SettingsData>()
    protected var settingsDataSource: Flow<SettingsData> = flowOf(settingsData)
    protected val mappedState = SettingsUiState(sortOrder = SortOrder.GIVEN_NAME_FIRST)
    protected val reloadedState = SettingsUiState(sortOrder = SortOrder.FAMILY_NAME_FIRST)

    private val simImportResultRepository = mockk<SimImportResultRepository>()
    private val profileRepository = mockk<ProfileRepository>()

    @Before
    fun setUpDefaultStubs() {
        every { getSettingsData() } answers { settingsDataSource }
        every { profileRepository.observeProfile() } returns profiles
        every { simImportResultRepository.observeSimImportResults() } returns simImportResults
        every { settingsUiStateMapper.map(settingsData = settingsData, profile = any()) } returns
            mappedState
        every {
            settingsUiStateMapper.map(settingsData = reloadedSettingsData, profile = any())
        } returns reloadedState
    }

    protected fun createViewModel(): SettingsViewModel {
        return SettingsViewModel(
            getSettingsData = getSettingsData,
            displaySettingsRepository = displaySettingsRepository,
            settingsUiStateMapper = settingsUiStateMapper,
            simImportResultRepository = simImportResultRepository,
            profileRepository = profileRepository,
        )
    }
}
