package com.android.contacts.data.contactdetails.mapper

import android.provider.ContactsContract.CommonDataKinds.Phone
import com.android.contacts.model.dataitem.DataItem
import com.android.contacts.tests.factory.collapsibleDataItem
import com.android.contacts.tests.factory.collapsibleDataKind
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class DataItemCollapserImplTest {

    private val collapseMatcher = mockk<DataItemCollapseMatcher>()
    private val collapser = DataItemCollapserImpl(collapseMatcher)

    @Before
    fun setUp() {
        every { collapseMatcher.shouldCollapse(any(), any()) } returns false
    }

    @Test
    fun collapse_withoutDataItems_returnsNoDataItems() {
        assertTrue(collapser.collapse(emptyList()).isEmpty())
    }

    @Test
    fun collapse_withDistinctDataItems_keepsThemInOrder() {
        val first = phone(id = 1L)
        val second = phone(id = 2L)

        assertEquals(listOf(1L, 2L), ids(collapser.collapse(listOf(first, second))))
    }

    @Test
    fun collapse_withCollapsibleDataItems_keepsTheEarlierItem() {
        val first = phone(id = 1L)
        val second = phone(id = 2L)
        every { collapseMatcher.shouldCollapse(first, second) } returns true

        assertEquals(listOf(1L), ids(collapser.collapse(listOf(first, second))))
    }

    @Test
    fun collapse_withLaterDataItemClaimingTheDuplicate_keepsTheLaterItem() {
        val first = phone(id = 1L)
        val second = phone(id = 2L)
        every { collapseMatcher.shouldCollapse(second, first) } returns true

        assertEquals(listOf(2L), ids(collapser.collapse(listOf(first, second))))
    }

    @Test
    fun collapse_withDuplicateClaimedInBothDirections_keepsTheEarlierItem() {
        val first = phone(id = 1L)
        val second = phone(id = 2L)
        every { collapseMatcher.shouldCollapse(first, second) } returns true
        every { collapseMatcher.shouldCollapse(second, first) } returns true

        assertEquals(listOf(1L), ids(collapser.collapse(listOf(first, second))))
    }

    @Test
    fun collapse_withDataItemsFollowingADroppedItem_keepsCollapsingTheSurvivor() {
        val items = (1L..3L).map { id -> phone(id = id) }
        every { collapseMatcher.shouldCollapse(items[1], items[0]) } returns true
        every { collapseMatcher.shouldCollapse(items[1], items[2]) } returns true

        assertEquals(listOf(2L), ids(collapser.collapse(items)))
    }

    @Test
    fun collapse_atTheCollapseLimit_collapsesDataItems() {
        val items = (1L..20L).map { id -> phone(id = id) }
        every { collapseMatcher.shouldCollapse(items[0], any()) } returns true

        assertEquals(listOf(1L), ids(collapser.collapse(items)))
    }

    @Test
    fun collapse_withMoreDataItemsThanTheCollapseLimit_returnsThemUnchanged() {
        val items = (1L..21L).map { id -> phone(id = id) }
        every { collapseMatcher.shouldCollapse(items[0], any()) } returns true

        assertEquals(items.map { item -> item.id }, ids(collapser.collapse(items)))
        verify(exactly = 0) { collapseMatcher.shouldCollapse(any(), any()) }
    }

    @Test
    fun collapse_withSuperPrimaryDuplicate_marksTheSurvivorPrimaryAndSuperPrimary() {
        val survivor = phone(id = 1L)
        val duplicate = phone(id = 2L, isSuperPrimary = true)
        every { collapseMatcher.shouldCollapse(survivor, duplicate) } returns true

        collapser.collapse(listOf(survivor, duplicate))

        assertTrue(survivor.isSuperPrimary)
        assertTrue(survivor.isPrimary)
    }

    @Test
    fun collapse_withPrimaryDuplicate_marksTheSurvivorPrimaryOnly() {
        val survivor = phone(id = 1L)
        val duplicate = phone(id = 2L, isPrimary = true)
        every { collapseMatcher.shouldCollapse(survivor, duplicate) } returns true

        collapser.collapse(listOf(survivor, duplicate))

        assertTrue(survivor.isPrimary)
        assertFalse(survivor.isSuperPrimary)
    }

    @Test
    fun collapse_withSuperPrimarySurvivor_alsoMarksItPrimary() {
        val survivor = phone(id = 1L, isSuperPrimary = true)
        val duplicate = phone(id = 2L)
        every { collapseMatcher.shouldCollapse(survivor, duplicate) } returns true

        collapser.collapse(listOf(survivor, duplicate))

        assertTrue(survivor.isSuperPrimary)
        assertTrue(survivor.isPrimary)
    }

    @Test
    fun collapse_whenTheLaterDataItemSurvives_mergesTheFlagsIntoIt() {
        val dropped = phone(id = 1L, isSuperPrimary = true)
        val survivor = phone(id = 2L)
        every { collapseMatcher.shouldCollapse(survivor, dropped) } returns true

        collapser.collapse(listOf(dropped, survivor))

        assertTrue(survivor.isSuperPrimary)
        assertTrue(survivor.isPrimary)
    }

    @Test
    fun collapse_withHigherPrecedenceDuplicateType_adoptsTheDuplicateType() {
        val survivor = phone(id = 1L, type = Phone.TYPE_OTHER)
        val duplicate = phone(id = 2L, type = Phone.TYPE_MOBILE)
        every { collapseMatcher.shouldCollapse(survivor, duplicate) } returns true

        collapser.collapse(listOf(survivor, duplicate))

        assertEquals(Phone.TYPE_MOBILE, type(survivor))
    }

    @Test
    fun collapse_withLowerPrecedenceDuplicateType_keepsTheSurvivorType() {
        val survivor = phone(id = 1L, type = Phone.TYPE_MOBILE)
        val duplicate = phone(id = 2L, type = Phone.TYPE_OTHER)
        every { collapseMatcher.shouldCollapse(survivor, duplicate) } returns true

        collapser.collapse(listOf(survivor, duplicate))

        assertEquals(Phone.TYPE_MOBILE, type(survivor))
    }

    @Test
    fun collapse_withoutTypeOnTheSurvivor_adoptsTheDuplicateType() {
        val survivor = phone(id = 1L, type = null)
        val duplicate = phone(id = 2L, type = Phone.TYPE_HOME)
        every { collapseMatcher.shouldCollapse(survivor, duplicate) } returns true

        collapser.collapse(listOf(survivor, duplicate))

        assertEquals(Phone.TYPE_HOME, type(survivor))
    }

    @Test
    fun collapse_withoutTypeOnTheDuplicate_keepsTheSurvivorType() {
        val survivor = phone(id = 1L, type = Phone.TYPE_HOME)
        val duplicate = phone(id = 2L, type = null)
        every { collapseMatcher.shouldCollapse(survivor, duplicate) } returns true

        collapser.collapse(listOf(survivor, duplicate))

        assertEquals(Phone.TYPE_HOME, type(survivor))
    }

    @Test
    fun collapse_withUnknownDuplicateType_keepsTheSurvivorType() {
        val survivor = phone(id = 1L, type = Phone.TYPE_OTHER)
        val duplicate = phone(id = 2L, type = UNKNOWN_TYPE)
        every { collapseMatcher.shouldCollapse(survivor, duplicate) } returns true

        collapser.collapse(listOf(survivor, duplicate))

        assertEquals(Phone.TYPE_OTHER, type(survivor))
    }

    @Test
    fun collapse_withLongerDuplicateMaxLines_keepsTheLargerMaxLines() {
        val survivor = phone(id = 1L, maxLinesForDisplay = 2)
        val duplicate = phone(id = 2L, maxLinesForDisplay = 5)
        every { collapseMatcher.shouldCollapse(survivor, duplicate) } returns true

        collapser.collapse(listOf(survivor, duplicate))

        assertEquals(5, survivor.dataKind.maxLinesForDisplay)
    }

    private fun ids(dataItems: List<DataItem>): List<Long> {
        return dataItems.map { dataItem -> dataItem.id }
    }

    private fun type(dataItem: DataItem): Int {
        return dataItem.getKindTypeColumn(dataItem.dataKind)
    }

    private fun phone(
        id: Long,
        type: Int? = Phone.TYPE_HOME,
        isPrimary: Boolean = false,
        isSuperPrimary: Boolean = false,
        maxLinesForDisplay: Int = 1,
    ): DataItem {
        return collapsibleDataItem(
            id = id,
            mimeType = Phone.CONTENT_ITEM_TYPE,
            data = "4155551212",
            type = type,
            isPrimary = isPrimary,
            isSuperPrimary = isSuperPrimary,
            dataKind = collapsibleDataKind(maxLinesForDisplay),
        )
    }

    private companion object {
        const val UNKNOWN_TYPE = 4242
    }
}
