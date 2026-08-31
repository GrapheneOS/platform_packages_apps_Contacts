package com.android.contacts.domain.contactdetails.model

internal data class ContactEntryContent(
    val header: ContactEntryText?,
    val subHeader: ContactEntryText? = null,
    val text: String? = null,
    val copyText: String? = null,
    val copyLabel: ContactEntryText? = null,
    val primaryAction: ContactEntryAction? = null,
    val alternateAction: ContactEntryAction? = null,
    val enhancedCallAction: ContactEntryAction? = null,
    val editBeforeCallAction: ContactEntryAction? = null,
) {

    fun isEmpty(): Boolean {
        return isTextEmpty(header) && isTextEmpty(subHeader) && text.isNullOrEmpty()
    }

    private fun isTextEmpty(entryText: ContactEntryText?): Boolean {
        return when (entryText) {
            null -> true
            is ContactEntryText.Label -> false
            is ContactEntryText.Value -> entryText.text.isEmpty()
        }
    }
}
