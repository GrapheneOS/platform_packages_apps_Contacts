package com.android.contacts.ui.debug

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.android.contacts.domain.debug.usecase.ClearSeededTestData
import com.android.contacts.domain.debug.usecase.ExportDatabase
import com.android.contacts.domain.debug.usecase.IsExportDatabaseAvailable
import com.android.contacts.domain.debug.usecase.SeedTestData
import com.android.contacts.ui.debug.model.DebugOption
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class DebugOptionsMenu @Inject constructor(
    private val isExportDatabaseAvailable: IsExportDatabaseAvailable,
    private val exportDatabase: ExportDatabase,
    private val seedTestData: SeedTestData,
    private val clearSeededTestData: ClearSeededTestData,
) {
    fun show(context: Context) {
        val optionsLabels = getOptions().map { context.getString(it.titleId) }.toTypedArray()
        AlertDialog.Builder(context)
            .setItems(optionsLabels) { _, which ->
                CoroutineScope(Dispatchers.Main).launch {
                    when (DebugOption.entries[which]) {
                        DebugOption.ExportDatabase -> exportDatabase()
                        DebugOption.SeedTestData -> seedTestData()
                        DebugOption.ClearSeededTestData -> clearSeededTestData()
                    }
                }
            }
            .show()
    }

    private fun getOptions(): List<DebugOption> {
        return DebugOption.entries
            .filter { it != DebugOption.ExportDatabase || isExportDatabaseAvailable() }
    }
}
