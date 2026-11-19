package com.android.contacts.domain.groups

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.android.contacts.domain.groups.model.GroupDeletion
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Singleton
internal class GroupEvents @Inject constructor() {

    private val _deleted = Channel<GroupDeletion>(Channel.BUFFERED)
    val deleted: Flow<GroupDeletion> = _deleted.receiveAsFlow()

    fun notifyDeleted(deletion: GroupDeletion) {
        _deleted.trySend(deletion)
    }

    // Temporary alternative for Java interoperability
    fun observeDeleted(owner: LifecycleOwner, callback: (GroupDeletion) -> Unit) {
        owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                deleted.collect { deletion -> callback(deletion) }
            }
        }
    }
}
