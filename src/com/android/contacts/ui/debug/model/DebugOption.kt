package com.android.contacts.ui.debug.model

import androidx.annotation.StringRes
import com.android.contacts.R

internal enum class DebugOption(
    @param:StringRes val titleId: Int,
) {
    ExportDatabase(R.string.menu_export_database),
    SeedTestData(R.string.debug_seed_test_data),
    ClearSeededTestData(R.string.debug_clear_seeded_test_data),
}
