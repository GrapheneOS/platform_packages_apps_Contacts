package com.android.contacts.ui.selection.screen.model

enum class SendToGroupTarget(
    val scheme: String,
) {
    SMS("smsto"),
    EMAIL("mailto"),
}
