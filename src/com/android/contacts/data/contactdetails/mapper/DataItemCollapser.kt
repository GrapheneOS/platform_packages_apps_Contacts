package com.android.contacts.data.contactdetails.mapper

import android.provider.ContactsContract.Data
import com.android.contacts.model.dataitem.DataItem
import javax.inject.Inject

internal interface DataItemCollapser {
    fun collapse(dataItems: List<DataItem>): List<DataItem>
}

internal class DataItemCollapserImpl @Inject constructor(
    private val collapseMatcher: DataItemCollapseMatcher,
) : DataItemCollapser {

    override fun collapse(dataItems: List<DataItem>): List<DataItem> {
        if (dataItems.size > MAX_COLLAPSIBLE_ITEMS) {
            return dataItems
        }

        val remaining: MutableList<DataItem?> = dataItems.toMutableList()
        for (index in remaining.indices) {
            val current = remaining[index] ?: continue
            collapseFollowing(remaining, index, current)
        }

        return remaining.filterNotNull()
    }

    private fun collapseFollowing(
        remaining: MutableList<DataItem?>,
        index: Int,
        current: DataItem,
    ) {
        for (otherIndex in index + 1 until remaining.size) {
            val other = remaining[otherIndex] ?: continue

            when {
                collapseMatcher.shouldCollapse(current, other) -> {
                    collapseInto(current, other)
                    remaining[otherIndex] = null
                }

                collapseMatcher.shouldCollapse(other, current) -> {
                    collapseInto(other, current)
                    remaining[index] = null
                    return
                }
            }
        }
    }

    private fun collapseInto(
        target: DataItem,
        other: DataItem,
    ) {
        val targetKind = target.dataKind
        val otherKind = other.dataKind

        if (adoptsOtherType(target, other)) {
            target.contentValues.put(otherKind.typeColumn, other.getKindTypeColumn(otherKind))
            target.dataKind = otherKind
        }

        target.dataKind.maxLinesForDisplay = maxOf(
            targetKind.maxLinesForDisplay,
            otherKind.maxLinesForDisplay,
        )

        if (target.isSuperPrimary || other.isSuperPrimary) {
            target.contentValues.put(Data.IS_SUPER_PRIMARY, 1)
            target.contentValues.put(Data.IS_PRIMARY, 1)
        }

        if (target.isPrimary || other.isPrimary) {
            target.contentValues.put(Data.IS_PRIMARY, 1)
        }
    }

    private fun adoptsOtherType(
        target: DataItem,
        other: DataItem,
    ): Boolean {
        return when {
            !other.hasKindTypeColumn(other.dataKind) -> false
            !target.hasKindTypeColumn(target.dataKind) -> true
            else -> typePrecedence(target) > typePrecedence(other)
        }
    }

    private fun typePrecedence(dataItem: DataItem): Int {
        val kind = dataItem.dataKind
        val rawValue = dataItem.getKindTypeColumn(kind)
        val index = kind.typeList.indexOfFirst { type -> type.rawValue == rawValue }

        return when (index) {
            NOT_FOUND -> Int.MAX_VALUE
            else -> index
        }
    }

    private companion object {
        const val MAX_COLLAPSIBLE_ITEMS = 20
        const val NOT_FOUND = -1
    }
}
