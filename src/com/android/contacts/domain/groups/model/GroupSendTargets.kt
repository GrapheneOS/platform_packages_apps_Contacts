package com.android.contacts.domain.groups.model

internal sealed interface GroupSendTargets {

    data object None : GroupSendTargets

    /**
     * Every contact with data has a default value, so the message can be sent directly.
     * [hasContactsWithoutValue] is `true` when some contacts were left out.
     */
    data class Direct(
        val values: List<String>,
        val hasContactsWithoutValue: Boolean,
    ) : GroupSendTargets

    /**
     * Some contacts have several values and none is the default, so the user needs to pick.
     * [defaultSelection] holds the data ids to pre-select.
     */
    data class NeedsPicker(
        val contactIds: Set<Long>,
        val defaultSelection: Set<Long>,
    ) : GroupSendTargets
}
