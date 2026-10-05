package com.android.contacts.ui.editor.springboard.screen.mapper

import android.content.Context
import com.android.contacts.R
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.contacts.model.RawContactWithAccount
import com.android.contacts.model.account.GoogleAccountType
import com.android.contacts.preference.ContactsPreferences
import com.android.contacts.ui.common.components.ContactAvatarImage
import com.android.contacts.ui.editor.springboard.screen.model.RawContactUiModel
import com.android.contacts.util.ContactDisplayUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal fun interface RawContactUiModelMapper {
    fun map(
        rawContact: RawContactWithAccount,
        isUserProfile: Boolean,
    ): RawContactUiModel
}

internal class RawContactUiModelMapperImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val contactsPreferences: ContactsPreferences,
) : RawContactUiModelMapper {
    override fun map(
        rawContact: RawContactWithAccount,
        isUserProfile: Boolean,
    ): RawContactUiModel {
        return RawContactUiModel(
            id = rawContact.id,
            avatarImage = rawContact.photoUri?.let(ContactAvatarImage::Uri),
            displayName = displayName(rawContact),
            accountLabel = accountLabel(rawContact.account, isUserProfile),
            accountIconData = rawContact.account.iconData,
        )
    }

    private fun displayName(rawContact: RawContactWithAccount): String {
        return ContactDisplayUtils.getPreferredDisplayName(
            rawContact.displayName,
            rawContact.displayNameAlt,
            contactsPreferences,
        )
            ?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.missing_name)
    }

    private fun accountLabel(
        account: AccountDisplayModel,
        isUserProfile: Boolean,
    ): String? {
        return when {
            isUserProfile && account.areContactsWritable ->
                accountProfileLabel(account)
            account.account.type == GoogleAccountType.ACCOUNT_TYPE &&
                account.account.dataSet == null ->
                account.name
            else ->
                account.type
        }
    }

    private fun accountProfileLabel(
        account: AccountDisplayModel,
    ): String? {
        return when {
            account.isDeviceAccount ->
                context.getString(R.string.local_profile_title)
            else ->
                context.getString(R.string.external_profile_title, account.type)
        }
    }
}
