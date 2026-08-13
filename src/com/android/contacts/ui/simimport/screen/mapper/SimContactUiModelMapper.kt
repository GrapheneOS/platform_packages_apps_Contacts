package com.android.contacts.ui.simimport.screen.mapper

import com.android.contacts.model.SimContact
import com.android.contacts.ui.simimport.screen.model.SimContactUiModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList

internal fun interface SimContactUiModelMapper {
    fun map(simContact: SimContact): SimContactUiModel
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
}
