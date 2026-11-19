package com.android.contacts.domain.groups

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import app.cash.turbine.test
import com.android.contacts.domain.groups.model.GroupDeletion
import com.android.contacts.tests.MainDispatcherRule
import com.android.contacts.tests.TestLifecycleOwner
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
internal class GroupEventsTest {

    private val subject = GroupEvents()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    @Test
    fun notifyDeleted_deliversTheDeletionToTheCollector() = runTest {
        val deletion = groupDeletion()

        subject.deleted.test {
            subject.notifyDeleted(deletion)

            assertEquals(deletion, awaitItem())
        }
    }

    @Test
    fun notifyDeleted_withoutCollector_buffersTheDeletionsInOrder() = runTest {
        val first = groupDeletion()
        val second = groupDeletion()

        subject.notifyDeleted(first)
        subject.notifyDeleted(second)

        subject.deleted.test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
        }
    }

    @Test
    fun deleted_deliversEachDeletionOnlyOnce() = runTest {
        val deletion = groupDeletion()
        subject.notifyDeleted(deletion)

        subject.deleted.test {
            assertEquals(deletion, awaitItem())
        }
        subject.deleted.test {
            expectNoEvents()
        }
    }

    @Test
    fun observeDeleted_beforeStarted_buffersUntilStarted() {
        val owner = TestLifecycleOwner(Lifecycle.State.CREATED)
        val received = mutableListOf<GroupDeletion>()
        val deletion = groupDeletion()
        subject.observeDeleted(owner) { received += it }

        subject.notifyDeleted(deletion)

        assertEquals(emptyList<GroupDeletion>(), received)

        owner.registry.currentState = Lifecycle.State.STARTED

        assertEquals(listOf(deletion), received)
    }

    @Test
    fun observeDeleted_whileStarted_deliversDeletions() {
        val owner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        val received = mutableListOf<GroupDeletion>()
        val deletion = groupDeletion()
        subject.observeDeleted(owner) { received += it }

        subject.notifyDeleted(deletion)

        assertEquals(listOf(deletion), received)
    }

    @Test
    fun observeDeleted_whenStopped_holdsDeletionsUntilStartedAgain() {
        val owner = TestLifecycleOwner(Lifecycle.State.STARTED)
        val received = mutableListOf<GroupDeletion>()
        val deletion = groupDeletion()
        subject.observeDeleted(owner) { received += it }
        owner.registry.currentState = Lifecycle.State.CREATED

        subject.notifyDeleted(deletion)

        assertEquals(emptyList<GroupDeletion>(), received)

        owner.registry.currentState = Lifecycle.State.STARTED

        assertEquals(listOf(deletion), received)
    }

    @Test
    fun observeDeleted_whenDestroyed_stopsDelivering() {
        val owner = TestLifecycleOwner(Lifecycle.State.STARTED)
        val received = mutableListOf<GroupDeletion>()
        subject.observeDeleted(owner) { received += it }
        owner.registry.currentState = Lifecycle.State.DESTROYED

        subject.notifyDeleted(groupDeletion())

        assertEquals(emptyList<GroupDeletion>(), received)
    }

    private fun groupDeletion() = GroupDeletion(undoData = Bundle())
}
