package com.android.contacts.ui.contactdetails.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsEffect as Effect
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsNavEvent as NavEvent
import com.android.contacts.ui.core.CollectEvents

@Composable
internal fun ContactDetailsScreen(
    onEffect: (Effect) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    screenModel: ContactDetailsScreenModel = viewModel<ContactDetailsViewModel>(),
) {
    val uiState by screenModel.uiState.collectAsStateWithLifecycle()

    CollectEvents(screenModel.effects) { effect ->
        onEffect(effect)
    }

    CollectEvents(screenModel.navigationEvents) { event ->
        when (event) {
            is NavEvent.Close -> onNavigateBack()
        }
    }

    ContactDetailsContent(
        uiState = uiState,
        onAction = screenModel::onAction,
        modifier = modifier,
    )
}
