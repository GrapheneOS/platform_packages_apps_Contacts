package com.android.contacts.tests

import com.android.contacts.ui.simimport.screen.model.SimContactUiModel
import kotlin.random.Random
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal object SimContactUiModelFactory {
    fun build(
        recordNumber: Int = Random.nextInt(),
        name: String = "Contact Name",
        phone: String? = null,
        emails: ImmutableList<String> = persistentListOf(),
    ) = SimContactUiModel(
        recordNumber,
        name,
        phone,
        emails,
    )
}
