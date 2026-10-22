package com.android.contacts.data.callsubject.repository

import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import com.android.contacts.di.core.IoDispatcher
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal interface CallSubjectRepository {
    suspend fun getHistory(): List<String>
    suspend fun addToHistory(subject: String)
}

internal class CallSubjectRepositoryImpl @Inject constructor(
    private val preferences: SharedPreferences,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : CallSubjectRepository {

    // More recent subjects come last
    override suspend fun getHistory(): List<String> {
        return withContext(coroutineDispatcher) {
            val count = preferences.getInt(PREF_KEY_HISTORY_COUNT, 0)

            (0 until count).mapNotNull { index ->
                preferences
                    .getString(PREF_KEY_HISTORY_ITEM + index, null)
                    ?.takeIf { it.isNotBlank() }
            }
                .takeLast(HISTORY_LIMIT)
        }
    }

    override suspend fun addToHistory(subject: String) {
        withContext(coroutineDispatcher) {
            if (subject.isBlank()) return@withContext

            val history = getHistory()
            val newHistory = if (history.contains(subject)) {
                // Move the subject to last
                history.filter { it != subject } + subject
            } else {
                history.takeLast(HISTORY_LIMIT - 1) + subject
            }

            preferences.edit {
                newHistory.forEachIndexed { index, subject ->
                    putString(PREF_KEY_HISTORY_ITEM + index, subject)
                }
                putInt(PREF_KEY_HISTORY_COUNT, newHistory.size)
            }
        }
    }

    companion object {
        @VisibleForTesting
        const val HISTORY_LIMIT = 5

        @VisibleForTesting
        const val PREF_KEY_HISTORY_COUNT = "subject_history_count"

        @VisibleForTesting
        const val PREF_KEY_HISTORY_ITEM = "subject_history_item"
    }
}
