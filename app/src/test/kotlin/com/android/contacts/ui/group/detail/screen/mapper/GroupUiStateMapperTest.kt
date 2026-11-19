package com.android.contacts.ui.group.detail.screen.mapper

import com.android.contacts.domain.contacts.model.ContactItem
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.domain.groups.model.GroupDetails
import com.android.contacts.tests.factory.ContactItemFactory
import com.android.contacts.tests.factory.GroupDetailsFactory
import com.android.contacts.tests.factory.GroupFactory
import com.android.contacts.tests.targetContext
import com.android.contacts.ui.common.mapper.ContactSectionedListMapper
import com.android.contacts.ui.common.model.ContactSectionedList
import com.android.contacts.ui.group.detail.screen.model.GroupContentState
import com.android.contacts.ui.group.detail.screen.model.GroupState
import com.android.contacts.ui.group.detail.screen.model.GroupUiState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentMapOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class GroupUiStateMapperTest {

    private val contactSectionedListMapper = mockk<ContactSectionedListMapper>()

    private val subject: GroupUiStateMapper = GroupUiStateMapperImpl(
        resources = targetContext.resources,
        contactSectionedListMapper = contactSectionedListMapper,
    )

    @Before
    fun setUp() {
        every {
            contactSectionedListMapper.map(
                contacts = any(),
                suggestions = any(),
                directories = any(),
                selectedKeys = any(),
                showFavorites = any(),
                showLetters = any(),
            )
        } returns SECTIONED_LIST
    }

    @Test
    fun map_whenLoading_isLoading() {
        assertEquals(GroupUiState.Loading, subject.map(GroupState(), GroupContentState.Loading))
    }

    @Test
    fun map_whenDeleted_isLoading() {
        assertEquals(GroupUiState.Loading, subject.map(GroupState(), GroupContentState.Deleted))
    }

    @Test
    fun map_whenLoaded_mapsTheGroup() {
        val result = map()

        assertEquals(
            GroupUiState.Content(
                title = "Friends",
                subtitle = "3 contacts",
                contacts = SECTIONED_LIST,
                isSelectionMode = false,
                canAddMembers = true,
                canRemoveMembers = false,
                canEditGroup = true,
                canSend = true,
                canSelectAll = true,
                deleteConfirmationMessage = null,
            ),
            result,
        )
        verify {
            contactSectionedListMapper.map(
                contacts = MEMBERS,
                suggestions = any(),
                directories = any(),
                selectedKeys = emptySet(),
                showFavorites = any(),
                showLetters = any(),
            )
        }
    }

    @Test
    fun subtitle_withOneMember_isSingular() {
        assertEquals("1 contact", map(members = listOf(MEMBER_1)).subtitle)
    }

    @Test
    fun map_withoutMembers_hasNoSubtitleAndCannotSend() {
        val result = map(members = emptyList())

        assertNull(result.subtitle)
        assertFalse(result.canSend)
        assertFalse(result.canSelectAll)
    }

    @Test
    fun map_whileSelecting_isInSelectionMode() {
        val result = map(state = GroupState(selectedKeys = setOf(MEMBER_1.key, MEMBER_2.key)))

        assertEquals("2 selected", result.title)
        assertNull(result.subtitle)
        assertTrue(result.isSelectionMode)
        assertFalse(result.canAddMembers)
        assertTrue(result.canRemoveMembers)
        assertFalse(result.canEditGroup)
        assertTrue(result.canSend)
        assertTrue(result.canSelectAll)
        verify {
            contactSectionedListMapper.map(
                contacts = MEMBERS,
                suggestions = any(),
                directories = any(),
                selectedKeys = setOf(MEMBER_1.key, MEMBER_2.key),
                showFavorites = any(),
                showLetters = any(),
            )
        }
    }

    @Test
    fun map_withEveryMemberSelected_cannotSelectAll() {
        val result = map(state = GroupState(selectedKeys = MEMBERS.map { it.key }.toSet()))

        assertEquals("3 selected", result.title)
        assertFalse(result.canSelectAll)
    }

    @Test
    fun map_withSelectedKeysNotInTheGroup_ignoresThem() {
        val result = map(state = GroupState(selectedKeys = setOf(ContactItemKey.Local(99L))))

        assertEquals("Friends", result.title)
        assertFalse(result.isSelectionMode)
        verify {
            contactSectionedListMapper.map(
                contacts = MEMBERS,
                suggestions = any(),
                directories = any(),
                selectedKeys = emptySet(),
                showFavorites = any(),
                showLetters = any(),
            )
        }
    }

    @Test
    fun map_withPartialStaleSelection_countsOnlyTheMembers() {
        val result = map(
            state = GroupState(selectedKeys = setOf(MEMBER_1.key, ContactItemKey.Local(99L))),
        )

        assertEquals("1 selected", result.title)
    }

    @Test
    fun map_whenMembershipIsNotEditable_cannotAddOrRemoveMembers() {
        val details = GroupDetailsFactory.build(group = GROUP, isMembershipEditable = false)

        assertFalse(map(details = details).canAddMembers)
        assertFalse(
            map(
                details = details,
                state = GroupState(selectedKeys = setOf(MEMBER_1.key)),
            ).canRemoveMembers,
        )
    }

    @Test
    fun map_whenTheGroupIsReadOnly_cannotEditTheGroup() {
        val details = GroupDetailsFactory.build(group = GROUP.copy(isReadOnly = true))

        assertFalse(map(details = details).canEditGroup)
    }

    @Test
    fun map_whenDeleteConfirmationIsVisible_hasTheConfirmationMessage() {
        val result = map(state = GroupState(isDeleteConfirmationVisible = true))

        assertEquals(
            "Delete the label \"Friends\"? (Contacts themselves will not be deleted.)",
            result.deleteConfirmationMessage,
        )
    }

    private fun map(
        state: GroupState = GroupState(),
        details: GroupDetails = DETAILS,
        members: List<ContactItem> = MEMBERS,
    ): GroupUiState.Content {
        return subject.map(state, GroupContentState.Loaded(details, members))
            as GroupUiState.Content
    }

    private companion object {
        val GROUP = GroupFactory.build(id = 7L, name = "Friends")
        val DETAILS = GroupDetailsFactory.build(group = GROUP)

        val MEMBER_1 = ContactItemFactory.build(key = ContactItemKey.Local(1L))
        val MEMBER_2 = ContactItemFactory.build(key = ContactItemKey.Local(2L))
        val MEMBER_3 = ContactItemFactory.build(key = ContactItemKey.Local(3L))
        val MEMBERS = listOf(MEMBER_1, MEMBER_2, MEMBER_3)

        val SECTIONED_LIST: ContactSectionedList = persistentMapOf()
    }
}
