package com.android.contacts.ui.interactions.callsubject.screen.model

internal sealed interface CallSubjectAction {
    data class SubjectChange(
        val value: String,
    ) : CallSubjectAction

    data class HistorySubjectPick(
        val subject: String,
    ) : CallSubjectAction

    data object CallClick : CallSubjectAction

    data object Dismiss : CallSubjectAction
}
