package com.android.contacts.domain.groups.usecase

import android.content.res.Resources
import com.android.contacts.R
import javax.inject.Inject

internal fun interface GetGroupNameMaxLenght {
    operator fun invoke(): Int
}

internal class GetGroupNameMaxLenghtImpl @Inject constructor(
    private val resources: Resources,
) : GetGroupNameMaxLenght {
    override fun invoke(): Int {
        return resources.getInteger(R.integer.group_name_max_length)
    }
}
