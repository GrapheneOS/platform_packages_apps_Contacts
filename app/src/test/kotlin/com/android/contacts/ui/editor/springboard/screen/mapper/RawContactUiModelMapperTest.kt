package com.android.contacts.ui.editor.springboard.screen.mapper

import com.android.contacts.R
import com.android.contacts.domain.accounts.model.AccountIconData
import com.android.contacts.model.account.GoogleAccountType
import com.android.contacts.preference.ContactsPreferences
import com.android.contacts.tests.AccountDisplayModelFactory
import com.android.contacts.tests.AccountModelFactory
import com.android.contacts.tests.RawContactWithAccountFactory
import com.android.contacts.ui.common.components.ContactAvatarImage
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
internal class RawContactUiModelMapperTest {

    private val context get() = RuntimeEnvironment.getApplication()
    private val contactsPreferences = mockk<ContactsPreferences>(relaxed = true)
    private val subject: RawContactUiModelMapper = RawContactUiModelMapperImpl(
        context = context,
        contactsPreferences = contactsPreferences,
    )

    @Test
    fun mapsId() {
        val rawContact = RawContactWithAccountFactory.build(id = 123L)

        assertEquals(
            rawContact.id,
            subject.map(rawContact, false).id,
        )
    }

    @Test
    fun mapsPhotoUriToAvatarImage() {
        val rawContact = RawContactWithAccountFactory.build(photoUri = "content://photo/1")

        assertEquals(
            rawContact.photoUri,
            (subject.map(rawContact, false).avatarImage as ContactAvatarImage.Uri)
                .value,
        )
    }

    @Test
    fun mapsAccountIconData() {
        val rawContact = RawContactWithAccountFactory.build(
            account = AccountDisplayModelFactory.build(
                iconData = AccountIconData(iconRes = R.drawable.accounts_empty),
            ),
        )

        assertEquals(
            rawContact.account.iconData,
            subject.map(rawContact, false).accountIconData,
        )
    }

    @Test
    fun whenPreferenceDisplayOrderIsPrimary_usesDisplayName() {
        every { contactsPreferences.displayOrder } returns ContactsPreferences.DISPLAY_ORDER_PRIMARY
        val rawContact = RawContactWithAccountFactory.build(
            displayName = "Name",
            displayNameAlt = "Alt",
        )

        assertEquals(
            rawContact.displayName,
            subject.map(rawContact, false).displayName,
        )
    }

    @Test
    fun whenPreferenceDisplayOrderIsAlternative_usesDisplayNameAlt() {
        every { contactsPreferences.displayOrder } returns
            ContactsPreferences.DISPLAY_ORDER_ALTERNATIVE
        val rawContact = RawContactWithAccountFactory.build(
            displayName = "Name",
            displayNameAlt = "Alt",
        )

        assertEquals(
            rawContact.displayNameAlt,
            subject.map(rawContact, false).displayName,
        )
    }

    @Test
    fun whenNamesAreBlank_usesFallback() {
        val rawContact = RawContactWithAccountFactory.build(displayName = "", displayNameAlt = "")

        assertEquals(
            context.getString(R.string.missing_name),
            subject.map(rawContact, false).displayName,
        )
    }

    @Test
    fun whenIsUserProfileAndAccountIsWritableAndDevice_useLocalProfileLabel() {
        val rawContact = RawContactWithAccountFactory.build(
            account = AccountDisplayModelFactory.build(
                areContactsWritable = true,
                isDeviceAccount = true,
            ),
        )

        assertEquals(
            context.getString(R.string.local_profile_title),
            subject.map(rawContact, isUserProfile = true).accountLabel,
        )
    }

    @Test
    fun whenIsUserProfileAndAccountIsWritableAndNotDevice_useRemoveProfileLabel() {
        val rawContact = RawContactWithAccountFactory.build(
            account = AccountDisplayModelFactory.build(
                type = "remote",
                areContactsWritable = true,
                isDeviceAccount = false,
            ),
        )

        assertEquals(
            context.getString(R.string.external_profile_title, rawContact.account.type),
            subject.map(rawContact, isUserProfile = true).accountLabel,
        )
    }

    @Test
    fun whenIsGoogleAccount_useAccountName() {
        val rawContact = RawContactWithAccountFactory.build(
            account = AccountDisplayModelFactory.build(
                name = "user@google.com",
                type = "Google",
                account = AccountModelFactory.build(
                    type = GoogleAccountType.ACCOUNT_TYPE,
                    dataSet = null,
                )
            ),
        )

        assertEquals(
            rawContact.account.name,
            subject.map(rawContact, isUserProfile = false).accountLabel,
        )
    }

    @Test
    fun whenIsOtherAccount_useAccountType() {
        val rawContact = RawContactWithAccountFactory.build(
            account = AccountDisplayModelFactory.build(
                name = "Account",
                type = "Type",
            ),
        )

        assertEquals(
            rawContact.account.type,
            subject.map(rawContact, isUserProfile = false).accountLabel,
        )
    }
}
