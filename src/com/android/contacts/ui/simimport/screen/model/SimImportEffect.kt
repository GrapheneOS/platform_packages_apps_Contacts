package com.android.contacts.ui.simimport.screen.model

internal sealed interface SimImportEffect {
    data object Close : SimImportEffect
}
