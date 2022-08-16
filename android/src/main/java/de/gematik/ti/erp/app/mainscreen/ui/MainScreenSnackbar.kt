/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.prescription.ui.GenerellErrorState
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceState
import de.gematik.ti.erp.app.prescription.ui.RefreshedState
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource

@Composable
fun MainScreenSnackbar(
    mainScreenViewModel: MainScreenViewModel,
    scaffoldState: ScaffoldState
) {
    var refreshEvent by remember { mutableStateOf<PrescriptionServiceState?>(null) }
    LaunchedEffect(Unit) {
        mainScreenViewModel.onRefreshEvent.collect {
            refreshEvent = it
        }
    }

    val refreshEventText = refreshEvent?.let {
        when (it) {
            GenerellErrorState.NetworkNotAvailable ->
                stringResource(R.string.error_message_network_not_available)
            is GenerellErrorState.ServerCommunicationFailedWhileRefreshing ->
                stringResource(R.string.error_message_server_communication_failed).format(it.code)
            GenerellErrorState.FatalTruststoreState ->
                stringResource(R.string.error_message_vau_error)
            is RefreshedState -> {
                if (it.nrOfNewPrescriptions == 0) {
                    stringResource(R.string.zero_prescriptions_updatet)
                } else {
                    annotatedPluralsResource(
                        R.plurals.prescriptions_updated,
                        quantity = it.nrOfNewPrescriptions,
                        AnnotatedString(it.nrOfNewPrescriptions.toString())
                    )
                }
            }
            else -> ""
        }
    }

    LaunchedEffect(refreshEventText) {
        try {
            refreshEventText?.let {
                scaffoldState.snackbarHostState.showSnackbar(refreshEventText.toString())
            }
        } finally {
            refreshEvent = null
        }
    }
}
