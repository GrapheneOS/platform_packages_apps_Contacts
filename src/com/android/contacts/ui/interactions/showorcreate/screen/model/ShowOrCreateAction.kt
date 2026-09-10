package com.android.contacts.ui.interactions.showorcreate.screen.model

internal sealed interface ShowOrCreateAction {
    data object CreateDismiss : ShowOrCreateAction
    data object CreateConfirm : ShowOrCreateAction
}
