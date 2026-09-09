package com.android.contacts.ui.interactions.importing.screen.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal data class ImportUiState(
    val isVCardImportAvailable: Boolean? = null,
    val simCardOptions: ImmutableList<SimCardOption>? = null,
) {
    val isLoading
        get() = isVCardImportAvailable == null || simCardOptions == null
    val noOptionsAvailable
        get() = isVCardImportAvailable == false && simCardOptions?.isEmpty() == true
}
