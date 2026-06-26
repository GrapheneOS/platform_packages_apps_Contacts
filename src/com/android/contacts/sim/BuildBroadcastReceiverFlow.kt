package com.android.contacts.sim

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/*
 * Build a hot flow that sends a Unit every time a broadcast is received for a certain IntentFilter.
 */
class BuildBroadcastReceiverFlow(private val localBroadcastManager: LocalBroadcastManager) {
    operator fun invoke(intentFilter: IntentFilter): Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(Unit)
            }
        }
        localBroadcastManager.registerReceiver(receiver, intentFilter)
        awaitClose { localBroadcastManager.unregisterReceiver(receiver) }
    }
}
