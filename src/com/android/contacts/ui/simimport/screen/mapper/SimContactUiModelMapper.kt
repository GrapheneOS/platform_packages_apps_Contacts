package com.android.contacts.ui.simimport.screen.mapper

import com.android.contacts.model.SimContact
import com.android.contacts.ui.simimport.screen.model.SimContactUiModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList

internal interface SimContactUiModelMapper {
    fun map(simContact: SimContact): SimContactUiModel
    fun unmap(uiModel: SimContactUiModel): SimContact
}

internal class SimContactUiModelMapperImpl @Inject constructor() : SimContactUiModelMapper {
    override fun map(simContact: SimContact): SimContactUiModel {
        return SimContactUiModel(
            recordNumber = simContact.recordNumber,
            name = simContact.name,
            phone = simContact.phone,
            emails = simContact.emails?.toImmutableList(),
        )
    }

    override fun unmap(uiModel: SimContactUiModel): SimContact {
        return SimContact(
            uiModel.recordNumber,
            uiModel.name,
            uiModel.phone,
            uiModel.emails?.toTypedArray(),
        )
    }
}
