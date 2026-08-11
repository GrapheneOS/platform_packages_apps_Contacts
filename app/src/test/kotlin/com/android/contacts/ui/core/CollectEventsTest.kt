package com.android.contacts.ui.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
internal class CollectEventsTest {

    @Test
    fun emittedEvents_reachTheHandler() = runComposeUiTest {
        val events = MutableSharedFlow<String>(extraBufferCapacity = 1)
        val received = mutableListOf<String>()

        setContent {
            CollectEvents(events) { event -> received += event }
        }

        events.tryEmit("first")
        waitForIdle()

        assertEquals(listOf("first"), received)
    }

    @Test
    fun afterRecomposition_theLatestHandlerReceivesEvents() = runComposeUiTest {
        val events = MutableSharedFlow<String>(extraBufferCapacity = 1)
        val received = mutableListOf<String>()
        var handlerId by mutableStateOf(1)

        setContent {
            val currentHandlerId = handlerId

            CollectEvents(events) { event -> received += "$currentHandlerId:$event" }
        }

        handlerId = 2
        waitForIdle()
        events.tryEmit("event")
        waitForIdle()

        assertEquals(listOf("2:event"), received)
    }
}
