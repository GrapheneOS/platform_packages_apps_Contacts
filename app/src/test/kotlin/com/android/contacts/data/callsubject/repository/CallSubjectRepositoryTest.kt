package com.android.contacts.data.callsubject.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.android.contacts.data.callsubject.repository.CallSubjectRepositoryImpl.Companion.HISTORY_LIMIT
import com.android.contacts.data.callsubject.repository.CallSubjectRepositoryImpl.Companion.PREF_KEY_HISTORY_COUNT
import com.android.contacts.data.callsubject.repository.CallSubjectRepositoryImpl.Companion.PREF_KEY_HISTORY_ITEM
import com.android.contacts.tests.targetContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class CallSubjectRepositoryTest {

    private val preferences: SharedPreferences =
        targetContext.getSharedPreferences("call_subject_test", Context.MODE_PRIVATE)

    private val repository: CallSubjectRepository = CallSubjectRepositoryImpl(
        preferences = preferences,
        coroutineDispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun getHistory_whenNothingIsStored_isEmpty() = runTest {
        assertTrue(repository.getHistory().isEmpty())
    }

    @Test
    fun getHistory_whenSubjectsAreStored_returnsThem() = runTest {
        givenStoredHistory("First", "Second", "Third")

        assertEquals(listOf("First", "Second", "Third"), repository.getHistory())
    }

    @Test
    fun getHistory_whenBlankSubjectsAreStored_skipsThem() = runTest {
        givenStoredHistory("First", " ", "Third")

        assertEquals(listOf("First", "Third"), repository.getHistory())
    }

    @Test
    fun getHistory_whenMoreThanTheLimitIsStored_returnsTheMostRecent() = runTest {
        val subjects = (1..HISTORY_LIMIT + 2).map { "Subject $it" }
        givenStoredHistory(*subjects.toTypedArray())

        assertEquals(subjects.takeLast(HISTORY_LIMIT), repository.getHistory())
    }

    @Test
    fun addToHistory_whenEmpty_storesTheSubject() = runTest {
        repository.addToHistory("Running late")

        assertEquals(listOf("Running late"), repository.getHistory())
    }

    @Test
    fun addToHistory_whenNotEmpty_appendsTheSubjectLast() = runTest {
        givenStoredHistory("First", "Second")

        repository.addToHistory("Third")

        assertEquals(listOf("First", "Second", "Third"), repository.getHistory())
    }

    @Test
    fun addToHistory_whenBlank_isIgnored() = runTest {
        givenStoredHistory("First")

        repository.addToHistory("")
        repository.addToHistory("   ")

        assertEquals(listOf("First"), repository.getHistory())
    }

    @Test
    fun addToHistory_whenAlreadyStored_movesTheSubjectLast() = runTest {
        givenStoredHistory("First", "Second", "Third")

        repository.addToHistory("First")

        assertEquals(listOf("Second", "Third", "First"), repository.getHistory())
    }

    @Test
    fun addToHistory_whenFull_dropsTheOldestSubject() = runTest {
        val subjects = (1..HISTORY_LIMIT).map { "Subject $it" }
        givenStoredHistory(*subjects.toTypedArray())

        repository.addToHistory("Newest")

        assertEquals(subjects.drop(1) + "Newest", repository.getHistory())
    }

    @Test
    fun addToHistory_storesTheCount() = runTest {
        givenStoredHistory("First", "Second")

        repository.addToHistory("Third")

        assertEquals(3, preferences.getInt(PREF_KEY_HISTORY_COUNT, 0))
    }

    private fun givenStoredHistory(vararg subjects: String) {
        preferences.edit {
            subjects.forEachIndexed { index, subject ->
                putString(PREF_KEY_HISTORY_ITEM + index, subject)
            }
            putInt(PREF_KEY_HISTORY_COUNT, subjects.size)
        }
    }
}
