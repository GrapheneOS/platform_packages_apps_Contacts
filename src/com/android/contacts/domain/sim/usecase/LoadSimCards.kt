package com.android.contacts.domain.sim.usecase

import android.telephony.SubscriptionManager
import com.android.contacts.database.SimContactDao
import com.android.contacts.di.core.SimReadDispatcher
import com.android.contacts.model.SimCard
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

internal fun interface LoadSimCards {
    operator fun invoke(): Flow<List<SimCard>>
}

internal class LoadSimCardsImpl @Inject constructor(
    private val subscriptionManager: SubscriptionManager,
    private val simContactDao: SimContactDao,
    @param:SimReadDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : LoadSimCards {

    override operator fun invoke(): Flow<List<SimCard>> {
        return onSubcriptionsChange()
            .onStart { emit(Unit) }
            .map { simContactDao.simCards }
            .flowOn(coroutineDispatcher)
    }

    private fun onSubcriptionsChange(): Flow<Unit> {
        return callbackFlow {
            val listener = object : SubscriptionManager.OnSubscriptionsChangedListener() {
                override fun onSubscriptionsChanged() {
                    trySend(Unit)
                }
            }
            subscriptionManager.addOnSubscriptionsChangedListener(
                coroutineDispatcher.asExecutor(),
                listener,
            )
            awaitClose {
                subscriptionManager.removeOnSubscriptionsChangedListener(listener)
            }
        }
    }
}
