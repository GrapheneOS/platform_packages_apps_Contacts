package com.android.contacts.ui.common.mapper

import android.provider.ContactsContract.CommonDataKinds.Phone
import com.android.contacts.data.contactdetails.model.ContactPhoto
import com.android.contacts.domain.contacts.model.ContactItem
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.tests.targetContext
import com.android.contacts.ui.common.components.ContactAvatarImage
import com.android.contacts.ui.common.model.ContactUiModel
import java.nio.ByteBuffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ContactUiModelMapperTest {

    private val subject: ContactUiModelMapper = ContactUiModelMapperImpl(targetContext.resources)

    @Test
    fun map_mapsKeyAndName() {
        val result = subject.map(ContactItem(key = KEY, name = "Anna"))

        assertEquals(ContactUiModel(key = KEY, name = "Anna"), result)
    }

    @Test
    fun map_withBlankName_usesMissingName() {
        assertEquals("(No name)", subject.map(ContactItem(key = KEY, name = " ")).name)
    }

    @Test
    fun map_isSelected_isPassedThrough() {
        val contact = ContactItem(key = KEY, name = "Anna")

        assertFalse(subject.map(contact).isSelected)
        assertTrue(subject.map(contact, isSelected = true).isSelected)
    }

    @Test
    fun map_mapsPhotoToAvatarImage() {
        val bytes = ByteBuffer.wrap(byteArrayOf(1, 2, 3))

        assertEquals(
            ContactAvatarImage.Uri("content://photo/1"),
            avatarOf(ContactPhoto.Uri("content://photo/1")),
        )
        assertEquals(ContactAvatarImage.Bytes(bytes), avatarOf(ContactPhoto.Bytes(bytes)))
        assertNull(avatarOf(null))
    }

    @Test
    fun description_withValueOnly_isTheValue() {
        val field = ContactItem.Field(
            value = "john@example.com",
            kind = ContactItem.FieldKind.EMAIL,
        )

        assertEquals("john@example.com", descriptionOf(field))
    }

    @Test
    fun description_withType_prefixesTypeLabel() {
        val field = ContactItem.Field(
            value = "+1 555 0100",
            kind = ContactItem.FieldKind.PHONE,
            type = Phone.TYPE_MOBILE,
        )

        assertEquals("Mobile · +1 555 0100", descriptionOf(field))
    }

    @Test
    fun description_withCustomType_usesCustomLabel() {
        val field = ContactItem.Field(
            value = "+1 555 0100",
            kind = ContactItem.FieldKind.PHONE,
            type = Phone.TYPE_CUSTOM,
            label = "Boat",
        )

        assertEquals("Boat · +1 555 0100", descriptionOf(field))
    }

    @Test
    fun description_withUnspecifiedKind_ignoresType() {
        val field = ContactItem.Field(
            value = "value",
            kind = ContactItem.FieldKind.UNSPECIFIED,
            type = Phone.TYPE_MOBILE,
        )

        assertEquals("value", descriptionOf(field))
    }

    @Test
    fun description_withMultilineAddress_joinsLines() {
        val field = ContactItem.Field(
            value = "1600 Amphitheatre Parkway\n\n  Mountain View, CA  \n",
            kind = ContactItem.FieldKind.POSTAL_ADDRESS,
        )

        assertEquals("1600 Amphitheatre Parkway, Mountain View, CA", descriptionOf(field))
    }

    @Test
    fun description_withBlankValueOrNoField_isNull() {
        assertNull(
            descriptionOf(ContactItem.Field(value = "\n ", kind = ContactItem.FieldKind.EMAIL)),
        )
        assertNull(descriptionOf(null))
    }

    private fun descriptionOf(field: ContactItem.Field?): String? {
        return subject.map(ContactItem(key = KEY, name = "Anna", field = field)).description
    }

    private fun avatarOf(photo: ContactPhoto?): ContactAvatarImage? {
        return subject.map(ContactItem(key = KEY, name = "Anna", contactPhoto = photo)).avatarImage
    }

    private companion object {
        val KEY = ContactItemKey.Local(1L)
    }
}
