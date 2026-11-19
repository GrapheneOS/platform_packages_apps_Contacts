package com.android.contacts.domain.groups.usecase

import app.cash.turbine.test
import com.android.contacts.data.contacts.model.ContactColumn
import com.android.contacts.data.groups.repository.GroupMembersRepository
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.data.settings.model.DisplaySettings
import com.android.contacts.data.settings.model.PhoneticNameDisplay
import com.android.contacts.data.settings.model.SortOrder
import com.android.contacts.data.settings.repository.DisplaySettingsRepository
import com.android.contacts.domain.contacts.mapper.ContactItemMapper
import com.android.contacts.domain.contacts.model.ContactItem
import com.android.contacts.domain.contacts.model.ContactItemKey
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class GetGroupMembersTest {

    private val groupMembersRepository = mockk<GroupMembersRepository>()
    private val displaySettingsRepository = mockk<DisplaySettingsRepository>()
    private val contactItemMapper = mockk<ContactItemMapper>()
    private val subject: GetGroupMembers = GetGroupMembersImpl(
        groupMembersRepository = groupMembersRepository,
        displaySettingsRepository = displaySettingsRepository,
        contactItemMapper = contactItemMapper,
        defaultDispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun mapsTheMembersWithTheDisplayOrder() = runTest {
        val column = ContactColumn(id = 1L)
        val item = ContactItem(key = ContactItemKey.Local(1L), name = "Alice")
        every { displaySettingsRepository.observeDisplaySettings() } returns flowOf(
            DISPLAY_SETTINGS.copy(
                sortOrder = SortOrder.FAMILY_NAME_FIRST,
                displayOrder = DisplayOrder.FAMILY_NAME_FIRST,
            ),
        )
        every { groupMembersRepository.queryGroupMembers(any(), any()) } returns
            flowOf(listOf(column))
        every { contactItemMapper.map(column, DisplayOrder.FAMILY_NAME_FIRST) } returns item

        val result = subject(7L).first()

        assertEquals(listOf(item), result)
        verify { groupMembersRepository.queryGroupMembers(7L, SortOrder.FAMILY_NAME_FIRST) }
    }

    @Test
    fun whenTheSortOrderChanges_queriesAgain() = runTest {
        val settings = MutableStateFlow(DISPLAY_SETTINGS)
        every { displaySettingsRepository.observeDisplaySettings() } returns settings
        every { groupMembersRepository.queryGroupMembers(any(), any()) } returns
            flowOf(emptyList())

        subject(7L).test {
            awaitItem()
            settings.value = DISPLAY_SETTINGS.copy(sortOrder = SortOrder.FAMILY_NAME_FIRST)
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify { groupMembersRepository.queryGroupMembers(7L, SortOrder.GIVEN_NAME_FIRST) }
        verify { groupMembersRepository.queryGroupMembers(7L, SortOrder.FAMILY_NAME_FIRST) }
    }

    @Test
    fun whenUnrelatedSettingsChange_doesNotQueryAgain() = runTest {
        val settings = MutableStateFlow(DISPLAY_SETTINGS)
        every { displaySettingsRepository.observeDisplaySettings() } returns settings
        every { groupMembersRepository.queryGroupMembers(any(), any()) } returns
            flowOf(emptyList())

        subject(7L).test {
            awaitItem()
            settings.value = DISPLAY_SETTINGS.copy(
                phoneticNameDisplay = PhoneticNameDisplay.HIDE_IF_EMPTY,
            )
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 1) { groupMembersRepository.queryGroupMembers(any(), any()) }
    }

    private companion object {
        val DISPLAY_SETTINGS = DisplaySettings(
            sortOrder = SortOrder.GIVEN_NAME_FIRST,
            isSortOrderChangeable = true,
            displayOrder = DisplayOrder.GIVEN_NAME_FIRST,
            isDisplayOrderChangeable = true,
            phoneticNameDisplay = PhoneticNameDisplay.SHOW_ALWAYS,
            isPhoneticNameDisplayChangeable = true,
        )
    }
}
