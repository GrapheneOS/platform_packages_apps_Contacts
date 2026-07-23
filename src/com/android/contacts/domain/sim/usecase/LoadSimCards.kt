package com.android.contacts.domain.sim.usecase

import com.android.contacts.database.SimContactDao
import com.android.contacts.di.core.SimReadDispatcher
import com.android.contacts.model.SimCard
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal fun interface LoadSimCards {
    operator fun invoke(): Flow<List<SimCard>>
}

internal class LoadSimCardsImpl @Inject constructor(
    private val simContactDao: SimContactDao,
    @param:SimReadDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : LoadSimCards {

    override operator fun invoke(): Flow<List<SimCard>> =
        flow { emit(simContactDao.simCards) }
            .flowOn(coroutineDispatcher)
}
