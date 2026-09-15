package com.android.contacts.domain.sim.usecase

import android.os.UserManager
import javax.inject.Inject

internal fun interface CanUserImportFromSim {
    operator fun invoke(): Boolean
}

internal class CanUserImportFromSimImpl @Inject constructor(
    private val userManager: UserManager,
) : CanUserImportFromSim {
    override fun invoke(): Boolean {
        return userManager.isAdminUser || userManager.isSystemUser
    }
}
