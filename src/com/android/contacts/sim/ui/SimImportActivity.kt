/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.contacts.sim.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.contacts.model.SimCard
import com.android.contacts.sim.SimImportDependencies
import com.android.contacts.ui.core.AppScaffold
import com.android.contacts.ui.core.AppTheme

class SimImportActivity : ComponentActivity() {
    private val dependencies by lazy { SimImportDependencies(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val subscriptionId = intent.extras
            ?.getInt(EXTRA_SUBSCRIPTION_ID, SimCard.NO_SUBSCRIPTION_ID)
            ?: SimCard.NO_SUBSCRIPTION_ID

        setContent {
            AppTheme {
                AppScaffold {
                    val viewModel = viewModel { dependencies.viewModel(subscriptionId) }
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    SimImportScreen(
                        state = state,
                        onEvent = viewModel::onEvent,
                        onClose = { finish() },
                    )
                }
            }
        }
    }

    companion object {
        private const val EXTRA_SUBSCRIPTION_ID: String = "extraSubscriptionId"

        fun getIntent(context: Context, subscriptionId: Int?) =
            Intent(context, SimImportActivity::class.java)
                .putExtra(EXTRA_SUBSCRIPTION_ID, subscriptionId)
    }
}
