package com.android.contacts.data.simimport.model

internal sealed interface SimImportResult {

    data class Success(
        val importedCount: Int,
    ) : SimImportResult

    data object Failure : SimImportResult
}
