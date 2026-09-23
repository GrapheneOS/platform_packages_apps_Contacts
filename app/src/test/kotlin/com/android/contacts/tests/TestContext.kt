package com.android.contacts.tests

import android.content.Context
import org.robolectric.RuntimeEnvironment

internal val targetContext: Context
    get() {
        return RuntimeEnvironment.getApplication()
    }
