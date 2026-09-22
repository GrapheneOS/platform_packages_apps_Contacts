package com.android.contacts.domain.contactdetails.usecase

import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.GroupMembership
import android.provider.ContactsContract.CommonDataKinds.Identity
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.ContactsContract.CommonDataKinds.Note
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.Relation
import android.provider.ContactsContract.CommonDataKinds.SipAddress
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.CommonDataKinds.Website
import com.android.contacts.data.connectedapps.model.ConnectedApp
import com.android.contacts.data.connectedapps.repository.ConnectedAppsRepository
import com.android.contacts.data.contactdetails.model.ContactDataItem
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.domain.contactdetails.mapper.ContactEntryContentMapper
import com.android.contacts.domain.contactdetails.model.CONTACT_DATA_ITEM_PRIORITY
import com.android.contacts.domain.contactdetails.model.ContactConnectedApp
import com.android.contacts.domain.contactdetails.model.ContactDetailsCards
import com.android.contacts.domain.contactdetails.model.ContactEntry
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.domain.contactdetails.model.ContactEntryActions
import com.android.contacts.domain.contactdetails.model.ContactEntryGroup
import com.android.contacts.domain.contactdetails.model.ContactEntryKind
import com.android.contacts.domain.contactdetails.model.duplicatesDisplayName
import com.android.contacts.model.dataitem.CustomDataItem
import com.android.contacts.util.core.extension.trimmedOrNull
import javax.inject.Inject

internal fun interface BuildContactDetailsCards {
    operator fun invoke(
        details: ContactDetails,
        prioritizedMimeType: String?,
    ): ContactDetailsCards
}

internal class BuildContactDetailsCardsImpl @Inject constructor(
    private val contactEntryContentMapper: ContactEntryContentMapper,
    private val connectedAppsRepository: ConnectedAppsRepository,
) : BuildContactDetailsCards {

    override operator fun invoke(
        details: ContactDetails,
        prioritizedMimeType: String?,
    ): ContactDetailsCards {
        val headerNicknames = headerNicknames(details)
        val headerOrganizations = headerOrganizations(details)
        val groupMemberships = groupMemberships(details)
        val headerDataIds = (headerNicknames + headerOrganizations + groupMemberships)
            .map { dataItem -> dataItem.id }
            .toSet()

        val groups = details.dataItems
            .filterNot { dataItem -> dataItem.id in headerDataIds }
            .groupBy { dataItem -> dataItem.mimeType }
            .mapValues { group -> group.value.sortedWith(CONTACT_DATA_ITEM_PRIORITY) }

        val groupsByApp = buildContactCard(
            groups,
            details,
            prioritizedMimeType
        ).groupBy { group -> connectedApp(group) }

        return ContactDetailsCards(
            contactCard = groupsByApp[NO_CONNECTED_APP].orEmpty(),
            connectedApps = buildConnectedApps(groupsByApp),
            notes = buildNotes(groups, details),
            headerNicknames = headerNicknames.mapNotNull { dataItem -> dataItem.name },
            headerOrganizations = headerOrganizations.map { organization ->
                organizationParts(organization)
            },
            groups = details.groups,
        )
    }

    private fun headerNicknames(details: ContactDetails): List<ContactDataItem.Nickname> {
        return details.dataItems
            .filterIsInstance<ContactDataItem.Nickname>()
            .sortedWith(CONTACT_DATA_ITEM_PRIORITY)
            .filter { dataItem ->
                !dataItem.name.isNullOrBlank() && !dataItem.duplicatesDisplayName(details)
            }
    }

    private fun groupMemberships(details: ContactDetails): List<ContactDataItem> {
        return details.dataItems.filter { dataItem ->
            dataItem.mimeType == GroupMembership.CONTENT_ITEM_TYPE
        }
    }

    private fun organizationParts(organization: ContactDataItem.Organization): List<String> {
        return listOfNotNull(
            organization.title,
            organization.department,
            organization.company,
        ).mapNotNull { part -> part.trimmedOrNull() }
    }

    private fun headerOrganizations(details: ContactDetails): List<ContactDataItem.Organization> {
        return details.dataItems
            .filterIsInstance<ContactDataItem.Organization>()
            .sortedWith(CONTACT_DATA_ITEM_PRIORITY)
            .filter { dataItem -> organizationParts(dataItem).isNotEmpty() }
    }

    private fun buildContactCard(
        groups: Map<String, List<ContactDataItem>>,
        details: ContactDetails,
        prioritizedMimeType: String?,
    ): List<ContactEntryGroup> {
        return groups.keys
            .filterNot { mimeType -> mimeType == Note.CONTENT_ITEM_TYPE }
            .sortedBy { mimeType ->
                mimeTypeRank(mimeType, prioritizedMimeType)
            }
            .map { mimeType ->
                toGroup(
                    mimeType = mimeType,
                    dataItems = groups.getValue(mimeType),
                    details = details
                )
            }
            .filterNot { group -> group.entries.isEmpty() }
    }

    private fun buildConnectedApps(
        groupsByApp: Map<ConnectedApp?, List<ContactEntryGroup>>,
    ): List<ContactConnectedApp> {
        return groupsByApp.mapNotNull { (app, appGroups) ->
            app?.let {
                ContactConnectedApp(
                    app = app,
                    entries = appGroups.flatMap { group -> group.entries },
                )
            }
        }
    }

    private fun connectedApp(group: ContactEntryGroup): ConnectedApp? {
        val entry = group.entries
            .firstOrNull()
            ?.takeIf { item -> item.kind == ContactEntryKind.OTHER }
        val action = entry?.actions?.primaryAction as? ContactEntryAction.ViewDataItem

        return when {
            action != null -> connectedAppsRepository.getConnectedApp(
                dataId = action.dataId,
                mimeType = action.mimeType,
            )

            else -> null
        }
    }

    private fun buildNotes(
        groups: Map<String, List<ContactDataItem>>,
        details: ContactDetails,
    ): List<ContactEntryGroup> {
        val notes = groups[Note.CONTENT_ITEM_TYPE] ?: return emptyList()

        return listOf(toGroup(Note.CONTENT_ITEM_TYPE, notes, details))
            .filterNot { group -> group.entries.isEmpty() }
    }

    private fun mimeTypeRank(
        mimeType: String,
        prioritizedMimeType: String?,
    ): Int {
        if (mimeType == prioritizedMimeType) {
            return PRIORITIZED_RANK
        }

        return when (val index = LEADING_MIME_TYPES.indexOf(mimeType)) {
            NOT_FOUND -> LEADING_MIME_TYPES.size
            else -> index
        }
    }

    private fun toGroup(
        mimeType: String,
        dataItems: List<ContactDataItem>,
        details: ContactDetails,
    ): ContactEntryGroup {
        val defaultDataItemId = defaultDataItemId(dataItems)

        return ContactEntryGroup(
            mimeType = mimeType,
            entries = dataItems.mapNotNull { dataItem ->
                toEntry(dataItem, details, defaultDataItemId)
            },
        )
    }

    private fun defaultDataItemId(dataItems: List<ContactDataItem>): Long? {
        return dataItems
            .firstOrNull { dataItem ->
                dataItem.isSuperPrimary || dataItem.isPrimary
            }?.id
    }

    private fun toEntry(
        dataItem: ContactDataItem,
        details: ContactDetails,
        defaultDataItemId: Long?,
    ): ContactEntry? {
        val content = contactEntryContentMapper.mapContent(dataItem, details)
            ?.takeIf { entry -> !entry.isEmpty() }
            ?: return null

        return ContactEntry(
            id = dataItem.id,
            mimeType = dataItem.mimeType,
            kind = contactEntryContentMapper.mapKind(dataItem),
            isSuperPrimary = dataItem.isSuperPrimary,
            isDefault = dataItem.id == defaultDataItemId,
            header = content.header,
            subHeader = content.subHeader,
            text = content.text,
            copyText = content.copyText,
            copyLabel = content.copyLabel ?: content.header,
            actions = ContactEntryActions(
                primaryAction = content.primaryAction,
                alternateAction = content.alternateAction,
                enhancedCallAction = content.enhancedCallAction,
                editBeforeCallAction = content.editBeforeCallAction,
            ),
        )
    }

    private companion object {
        val NO_CONNECTED_APP: ConnectedApp? = null

        const val NOT_FOUND = -1
        const val PRIORITIZED_RANK = -1

        val LEADING_MIME_TYPES = listOf(
            Phone.CONTENT_ITEM_TYPE,
            Email.CONTENT_ITEM_TYPE,
            StructuredPostal.CONTENT_ITEM_TYPE,
            Event.CONTENT_ITEM_TYPE,
            Website.CONTENT_ITEM_TYPE,
            SipAddress.CONTENT_ITEM_TYPE,
            Relation.CONTENT_ITEM_TYPE,
            Im.CONTENT_ITEM_TYPE,
            Identity.CONTENT_ITEM_TYPE,
            CustomDataItem.MIMETYPE_CUSTOM_FIELD,
        )
    }
}
