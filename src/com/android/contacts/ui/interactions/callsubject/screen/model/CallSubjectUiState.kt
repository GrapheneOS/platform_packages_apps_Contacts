package com.android.contacts.ui.interactions.callsubject.screen.model

import androidx.compose.runtime.Immutable
import com.android.contacts.data.callsubject.model.DEFAULT_CALL_SUBJECT_LIMIT
import com.android.contacts.ui.common.components.ContactAvatarImage
import java.nio.charset.Charset
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
internal data class CallSubjectUiState(
    val avatarImage: ContactAvatarImage?,
    val isBusiness: Boolean,
    val name: String,
    val number: String?,
    val numberLabel: String?,
    val subject: String = "",
    val subjectLimit: Int = DEFAULT_CALL_SUBJECT_LIMIT,
    val subjectEncoding: Charset? = null,
    val history: ImmutableList<String> = persistentListOf(),
) {
    val subjectSize = when (subjectEncoding) {
        null -> subject.length
        else -> subject.toByteArray(subjectEncoding).size
    }
    val isHistoryAvailable get() = history.isNotEmpty()
}
