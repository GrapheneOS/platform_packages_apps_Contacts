package com.android.contacts.ui.group.list.screen.mapper

import android.content.res.Resources
import com.android.contacts.domain.accounts.mapper.AccountDisplayModelMapper
import com.android.contacts.domain.groups.model.Group
import com.android.contacts.tests.AccountDisplayModelFactory
import com.android.contacts.tests.AccountModelFactory
import com.android.contacts.ui.group.list.screen.model.GroupUiItem
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test

class GroupsUiMapperTest {

    private val accountDisplayModelMapper = mockk<AccountDisplayModelMapper>()
    private val resources = mockk<Resources>()
    private val subject: GroupsUiMapper = GroupsUiMapperImpl(
        accountDisplayModelMapper = accountDisplayModelMapper,
        resources = resources,
    )

    @Test
    fun mapsFieldsCorrectly() {
        val account = AccountModelFactory.build(name = "Name")
        val accountDisplayModel = AccountDisplayModelFactory.build(
            account = account,
            name = "Display Name",
            areContactsWritable = true,
        )
        every { accountDisplayModelMapper.map(account) } returns accountDisplayModel
        val group = Group(
            id = 123L,
            name = "Group",
            summaryCount = 20,
            systemId = "group_system_id",
            account = account,
            isReadOnly = false,
        )

        val result = subject.map(listOf(group))

        assertEquals(1, result.size)
        val accountGroup = result.first()
        assertEquals(account, accountGroup.account)
        assertEquals(accountDisplayModel.name, accountGroup.accountName)
        assertEquals(true, accountGroup.canCreateGroup)
        assertEquals(
            persistentListOf(
                GroupUiItem(
                    id = 123L,
                    name = "Group",
                    summaryCount = 20,
                ),
            ),
            accountGroup.groups,
        )
    }
}
