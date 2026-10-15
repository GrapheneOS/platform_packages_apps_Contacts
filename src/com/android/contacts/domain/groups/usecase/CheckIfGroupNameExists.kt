package com.android.contacts.domain.groups.usecase

import com.android.contacts.domain.accounts.model.AccountModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first

internal fun interface CheckIfGroupNameExists {
    suspend operator fun invoke(account: AccountModel, groupName: String): Boolean
}

internal class CheckIfGroupNameExistsImpl @Inject constructor(
    private val getGroups: GetGroups,
) : CheckIfGroupNameExists {
    override suspend fun invoke(
        account: AccountModel,
        groupName: String,
    ): Boolean {
        val groupNameTrimmed = groupName.trim()
        val existingGroups = getGroups(account).first().orEmpty()
        // When "Group" exists, do not save " Group ". This behavior is the same as Google Contacts.
        return existingGroups.any { it.name.trim() == groupNameTrimmed }
    }
}
