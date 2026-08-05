package com.android.contacts.domain.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/*
 * Build a hot flow that sends a Unit every time a broadcast is received for a certain IntentFilter.
 */
internal fun interface BuildBroadcastReceiverFlow {
    operator fun invoke(intentFilter: IntentFilter): Flow<Unit>
}

internal class BuildBroadcastReceiverFlowImpl @Inject constructor(
    private val localBroadcastManager: LocalBroadcastManager,
) : BuildBroadcastReceiverFlow {
    override operator fun invoke(intentFilter: IntentFilter): Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(Unit)
            }
        }
        localBroadcastManager.registerReceiver(receiver, intentFilter)
        awaitClose { localBroadcastManager.unregisterReceiver(receiver) }
    }
}
