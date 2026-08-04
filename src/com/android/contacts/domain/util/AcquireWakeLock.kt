package com.android.contacts.domain.util

import android.os.PowerManager
import javax.inject.Inject
import kotlin.time.Duration

internal fun interface AcquireWakeLock {
    operator fun invoke(
        tag: String,
        timeout: Duration,
    ): Wrapper

    interface Wrapper {
        fun releaseIfHeld()
    }
}

internal class AcquireWakeLockImpl @Inject constructor(
    private val powerManager: PowerManager,
) : AcquireWakeLock {
    override operator fun invoke(
        tag: String,
        timeout: Duration,
    ): AcquireWakeLock.Wrapper {
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
            tag,
        )
        wakeLock.acquire(timeout.inWholeMilliseconds)
        return object : AcquireWakeLock.Wrapper {
            override fun releaseIfHeld() {
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
            }
        }
    }
}
