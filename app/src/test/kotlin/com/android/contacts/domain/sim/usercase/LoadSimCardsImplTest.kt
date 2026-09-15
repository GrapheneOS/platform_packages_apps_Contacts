package com.android.contacts.domain.sim.usercase

import android.telephony.SubscriptionManager
import app.cash.turbine.test
import com.android.contacts.database.SimContactDao
import com.android.contacts.domain.sim.usecase.CanUserImportFromSim
import com.android.contacts.domain.sim.usecase.LoadSimCardsImpl
import com.android.contacts.model.SimCard
import com.android.contacts.tests.factory.SimCardFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoadSimCardsImplTest {

    private val canUserImportFromSim = mockk<CanUserImportFromSim>()
    private val subscriptionManager = mockk<SubscriptionManager>(relaxed = true)
    private val changeListenerSlot = slot<SubscriptionManager.OnSubscriptionsChangedListener>()
    private val simContactDao = mockk<SimContactDao>(relaxed = true)

    private val useCase = LoadSimCardsImpl(
        canUserImportFromSim = canUserImportFromSim,
        subscriptionManager = subscriptionManager,
        simContactDao = simContactDao,
        coroutineDispatcher = UnconfinedTestDispatcher(),
    )

    @Before
    fun setUp() {
        every { canUserImportFromSim() } returns true
        every {
            subscriptionManager.addOnSubscriptionsChangedListener(
                any(),
                capture(changeListenerSlot),
            )
        } answers {
            changeListenerSlot.captured.onSubscriptionsChanged()
        }
        every { simContactDao.simCards } returns emptyList<SimCard>()
    }

    @Test
    fun whenCannotImport_returnEmptyListAndCallNothingElse() = runTest {
        every { canUserImportFromSim() } returns false

        useCase().test {
            assertEquals(emptyList<SimCard>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 0) { subscriptionManager.addOnSubscriptionsChangedListener(any(), any()) }
        verify(exactly = 0) { simContactDao.simCards }
    }

    @Test
    fun loadsSimCards() = runTest {
        val simCard = SimCardFactory.build(subscriptionId = 1)
        every { simContactDao.simCards } returns listOf(simCard)

        useCase().test {
            assertEquals(listOf(simCard), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onLoadUnsupportedOperationException_returnEmptyList() = runTest {
        every { simContactDao.simCards } throws UnsupportedOperationException("")

        useCase().test {
            assertEquals(emptyList<SimCard>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onEverySubscriptionChange_loadSimCards() = runTest {
        useCase().test {
            awaitItem()
            changeListenerSlot.captured.onSubscriptionsChanged()
            awaitItem()
            changeListenerSlot.captured.onSubscriptionsChanged()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 3) { simContactDao.simCards }
    }
}
