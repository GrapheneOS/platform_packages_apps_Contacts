package com.android.contacts.domain.groups.usecase

import android.app.Activity
import android.content.Context
import com.android.contacts.ContactSaveService
import com.android.contacts.domain.accounts.mapper.AccountModelMapper
import com.android.contacts.domain.accounts.model.AccountModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal fun interface CreateOrEditGroupName {
    operator fun invoke(
        account: AccountModel?,
        groupName: String,
        groupId: Long?,
        callbackActivity: Class<out Activity>,
        callbackAction: String?,
    )
}

internal class CreateOrEditGroupNameImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val accountModelMapper: AccountModelMapper,
) : CreateOrEditGroupName {
    override fun invoke(
        account: AccountModel?,
        groupName: String,
        groupId: Long?,
        callbackActivity: Class<out Activity>,
        callbackAction: String?,
    ) {
        val intent = when {
            groupId != null ->
                ContactSaveService.createGroupRenameIntent(
                    context,
                    groupId,
                    groupName,
                    callbackActivity,
                    callbackAction,
                )
            else ->
                ContactSaveService.createNewGroupIntent(
                    context,
                    account?.let(accountModelMapper::map),
                    groupName,
                    null,
                    callbackActivity,
                    callbackAction,
                )
        }
        ContactSaveService.startService(context, intent)
    }
}
