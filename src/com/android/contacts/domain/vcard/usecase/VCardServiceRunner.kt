package com.android.contacts.domain.vcard.usecase

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.android.contacts.vcard.VCardService
import com.android.contacts.vcard.VCardService.MyBinder
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal fun interface VCardServiceRunner {
    operator fun invoke(): Flow<VCardService>
}

internal class VCardServiceRunnerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : VCardServiceRunner {
    override fun invoke(): Flow<VCardService> {
        return callbackFlow {
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, binder: IBinder) {
                    trySend((binder as MyBinder).service)
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    close()
                }
            }

            val intent = Intent(context, VCardService::class.java)
            context.startService(intent)
            context.bindService(
                Intent(context, VCardService::class.java),
                connection,
                Context.BIND_AUTO_CREATE,
            )

            awaitClose {
                try {
                    context.unbindService(connection)
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "Cannot unbind service connection", e)
                }
            }
        }
    }

    private companion object {
        const val TAG = "VCardServiceRunner"
    }
}
