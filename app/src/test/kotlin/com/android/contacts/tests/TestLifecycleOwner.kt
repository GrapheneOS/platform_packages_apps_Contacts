package com.android.contacts.tests

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

class TestLifecycleOwner(
    initialState: Lifecycle.State = Lifecycle.State.RESUMED,
) : LifecycleOwner {

    val registry = LifecycleRegistry.createUnsafe(this).apply {
        currentState = initialState
    }

    override val lifecycle: Lifecycle
        get() = registry
}
