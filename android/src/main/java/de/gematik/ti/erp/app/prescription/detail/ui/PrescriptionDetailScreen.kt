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

package de.gematik.ti.erp.app.prescription.detail.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.api.Result
import de.gematik.ti.erp.app.db.entities.LowDetailEventSimple
import de.gematik.ti.erp.app.db.entities.TaskStatus
import de.gematik.ti.erp.app.mainscreen.ui.MainNavigationScreens
import de.gematik.ti.erp.app.mainscreen.ui.TaskIds
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionDetailsNavigationScreens
import de.gematik.ti.erp.app.prescription.detail.ui.model.UIPrescriptionDetail
import de.gematik.ti.erp.app.prescription.detail.ui.model.UIPrescriptionDetailScanned
import de.gematik.ti.erp.app.prescription.detail.ui.model.UIPrescriptionDetailSynced
import de.gematik.ti.erp.app.prescription.repository.InsuranceCompanyDetail
import de.gematik.ti.erp.app.prescription.repository.MedicationRequestDetail
import de.gematik.ti.erp.app.prescription.repository.OrganizationDetail
import de.gematik.ti.erp.app.prescription.repository.PatientDetail
import de.gematik.ti.erp.app.prescription.repository.PractitionerDetail
import de.gematik.ti.erp.app.prescription.repository.codeToDosageFormMapping
import de.gematik.ti.erp.app.prescription.ui.expiryOrAcceptString
import de.gematik.ti.erp.app.redeem.ui.BitMatrixCode
import de.gematik.ti.erp.app.redeem.ui.DataMatrixCode
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AlertDialog
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.HintCard
import de.gematik.ti.erp.app.utils.compose.HintCardDefaults
import de.gematik.ti.erp.app.utils.compose.HintSmallImage
import de.gematik.ti.erp.app.utils.compose.HintTextActionButton
import de.gematik.ti.erp.app.utils.compose.HintTextLearnMoreButton
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.Spacer16
import de.gematik.ti.erp.app.utils.compose.Spacer4
import de.gematik.ti.erp.app.utils.compose.Spacer8
import de.gematik.ti.erp.app.utils.compose.annotatedLinkStringLight
import de.gematik.ti.erp.app.utils.compose.createToastShort
import de.gematik.ti.erp.app.utils.compose.navigationModeState
import de.gematik.ti.erp.app.utils.compose.phrasedDateString
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private const val MISSING_VALUE = "---"
private const val FORBIDDEN = 403

@Composable
fun PrescriptionDetailsScreen(
    taskId: String,
    mainNavController: NavController,
    viewModel: PrescriptionDetailsViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    val navigationMode by navController.navigationModeState(PrescriptionDetailsNavigationScreens.PrescriptionDetails.route)
    NavHost(
        navController,
        startDestination = PrescriptionDetailsNavigationScreens.PrescriptionDetails.route
    ) {
        composable(PrescriptionDetailsNavigationScreens.PrescriptionDetails.route) {
            PrescriptionDetailsWithScaffold(
                mainNavController,
                viewModel,
                taskId,
                navigationMode,
                onCancel = { mainNavController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PrescriptionDetailsWithScaffold(
    mainNavController: NavController,
    viewModel: PrescriptionDetailsViewModel,
    taskId: String,
    navigationMode: NavigationMode,
    onCancel: () -> Unit
) {
    val state by produceState<UIPrescriptionDetail?>(null) {
        value = viewModel.detailedPrescription(taskId)
    }

    val lowDetailRedeemEvents by produceState<List<LowDetailEventSimple>>(mutableListOf()) {
        viewModel.loadLowDetailEvents(taskId).collect {
            value = it
        }
    }

    val header = stringResource(id = R.string.prescription_details)
    Scaffold(
        topBar = {
            NavigationTopAppBar(
                NavigationBarMode.Close,
                title = header,
                onBack = onCancel
            )
        },
    ) { innerPadding ->
        Box(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            state?.let {
                NavigationAnimation(mode = navigationMode) {
                    PrescriptionDetails(
                        viewModel = viewModel,
                        mainNavController = mainNavController,
                        state = it,
                        lowDetailRedeemEvents = lowDetailRedeemEvents,
                        onCancel = onCancel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PrescriptionDetails(
    modifier: Modifier = Modifier,
    mainNavController: NavController,
    viewModel: PrescriptionDetailsViewModel,
    state: UIPrescriptionDetail,
    lowDetailRedeemEvents: List<LowDetailEventSimple>,
    onCancel: () -> Unit
) {
    var showMore by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val hintPadding = Modifier.padding(start = PaddingDefaults.Medium, end = PaddingDefaults.Medium)

    val isSubstituted = (state as? UIPrescriptionDetailSynced)?.let {
        it.medicationDispense != null && it.medication.uniqueIdentifier != it.medicationDispense.uniqueIdentifier
    } ?: false

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize(),
        contentPadding = rememberInsetsPaddingValues(
            insets = LocalWindowInsets.current.navigationBars,
            applyBottom = true
        )
    ) {
        if ((state as? UIPrescriptionDetailSynced)?.medicationRequest?.emergencyFee == true && state.redeemedOn == null) {
            item {
                EmergencyServiceCard()
            }
        }

        // low detail redeemed information
        if (state is UIPrescriptionDetailScanned && (state as? UIPrescriptionDetailScanned)?.redeemedOn != null) {
            item {
                Spacer16()
                HintCard(
                    modifier = hintPadding,
                    image = {
                        HintSmallImage(
                            painterResource(R.drawable.health_card_hint_blue),
                            innerPadding = it
                        )
                    },
                    title = { Text(stringResource(R.string.scanned_prescription_detail_redeemed_hint_header)) },
                    body = { Text(stringResource(R.string.scanned_prescription_detail_redeemed_hint_info)) },
                    action = {
                        HintTextActionButton(stringResource(R.string.scanned_prescription_detail_redeemed_hint_connect)) {
                            mainNavController.navigate(MainNavigationScreens.CardWall.path())
                        }
                    }
                )
            }
        }

        // low detail information
        if (state is UIPrescriptionDetailScanned && (state as? UIPrescriptionDetailScanned)?.redeemedOn == null) {
            item {
                Spacer16()
                HintCard(
                    modifier = hintPadding,
                    image = {
                        HintSmallImage(
                            painterResource(R.drawable.information),
                            innerPadding = it
                        )
                    },
                    title = { Text(stringResource(R.string.scanned_prescription_detail_info_hint_header)) },
                    body = { Text(stringResource(R.string.scanned_prescription_detail_info_hint_info)) },
                    action = {
                        HintTextLearnMoreButton()
                    }
                )
            }
        }

        state.bitmapMatrix?.let { bitMatrix ->
            if (state.redeemedOn == null) {
                item {
                    DataMatrixCode(bitMatrix)
                }
            }
        }

        item {
            val prescriptionName = when (state) {
                is UIPrescriptionDetailScanned -> stringResource(
                    id = R.string.scanned_prescription_placeholder_name,
                    state.number
                )
                is UIPrescriptionDetailSynced -> when (isSubstituted) {
                    true -> state.medicationDispense?.text ?: MISSING_VALUE
                    false -> state.medication.text ?: MISSING_VALUE
                }
                else -> MISSING_VALUE
            }

            Header(
                text = prescriptionName
            )
        }

        val taskId = state.taskId
        when (state) {
            is UIPrescriptionDetailSynced -> item {
                FullDetailSecondHeader(state) {
                    mainNavController.navigate(
                        MainNavigationScreens.Pharmacies.path(
                            taskIds = TaskIds(
                                listOf(taskId)
                            )
                        )
                    )
                }
            }
            is UIPrescriptionDetailScanned -> item {
                LowDetailRedeemHeader(state) { redeem, all, protocolText ->
                    viewModel.onSwitchRedeemed(state.taskId, redeem, all, protocolText)
                }
            }
        }

        if ((state as? UIPrescriptionDetailSynced)?.medicationRequest?.substitutionAllowed == true && state.redeemedOn == null) {
            item {
                SubstitutionAllowed()
            }
        }

        item {
            Column {
                if (state is UIPrescriptionDetailScanned) {
                    ProtocolScanned(state, lowDetailRedeemEvents)
                }
                if (state is UIPrescriptionDetailSynced) {
                    MedicationInformation(state, isSubstituted)
                    if (isSubstituted) {
                        WasSubstitutedHint()
                    }
                    DosageInformation(state, isSubstituted)
                    HealthPortalLink()
                    PatientInformation(state.patient, state.insurance)
                }
            }
        }

        item {
            Column(modifier = Modifier.fillMaxSize()) {
                Button(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 40.dp, bottom = 40.dp)
                        .toggleable(
                            value = showMore,
                            onValueChange = { showMore = it },
                            role = Role.Checkbox,
                        ),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = AppTheme.colors.neutral050,
                        contentColor = AppTheme.colors.primary700
                    ),
                    onClick = { showMore = !showMore }
                ) {
                    Text(
                        stringResource(
                            when (showMore) {
                                true -> R.string.pres_detail_show_less
                                false -> R.string.pres_detail_show_more
                            }
                        ).uppercase(Locale.getDefault())
                    )
                    Icon(
                        imageVector = when (showMore) {
                            true -> Icons.Rounded.KeyboardArrowUp
                            false -> Icons.Rounded.KeyboardArrowDown
                        },
                        contentDescription = null
                    )
                }

                AnimatedVisibility(
                    visibleState = remember { MutableTransitionState(false) }.apply {
                        targetState = showMore
                    },
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column {
                        if (state is UIPrescriptionDetailSynced) {
                            PractitionerInformation(state.practitioner)
                            OrganizationInformation(state.organization)
                            AccidentInformation(state.medicationRequest)
                        }
                        TechnicalPrescriptionInformation(
                            accessCode = state.accessCode,
                            taskId = state.taskId
                        )

                        val context = LocalContext.current
                        val accessInfoText = stringResource(R.string.logout_delete_no_access)
                        val prescriptionInProgressText =
                            stringResource(R.string.logout_delete_in_progress)

                        DeleteButton(state is UIPrescriptionDetailSynced) {
                            viewModel.deletePrescription(
                                state.taskId,
                                state is UIPrescriptionDetailSynced
                            ).apply {
                                if (this is Result.Error) {
                                    if (this.exception is ApiCallException) {
                                        if (this.exception.response.code() == FORBIDDEN) {
                                            createToastShort(context, prescriptionInProgressText)
                                        } else {
                                            createToastShort(context, accessInfoText)
                                        }
                                    } else {
                                        createToastShort(context, accessInfoText)
                                    }
                                } else {
                                    onCancel()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val scrollOffset = with(LocalDensity.current) {
        200.dp.toPx()
    }

    LaunchedEffect(showMore) {
        if (showMore) {
            delay(100)
            listState.animateScrollBy(scrollOffset)
        }
    }
}

@Composable
private fun DataMatrixCode(bitmapMatrix: BitMatrixCode) {
    Surface(
        shape = RoundedCornerShape(PaddingDefaults.Medium / 2),
        border = BorderStroke(1.dp, AppTheme.colors.neutral300),
        modifier = Modifier.padding(16.dp)
    ) {
        DataMatrixCode(
            bitmapMatrix,
            modifier = Modifier
                .aspectRatio(1.0f)
        )
    }
}

@Composable
private fun DeleteButton(isSyncedPrescription: Boolean, onClickDelete: () -> Unit) {
    var showDeletePrescriptionDialog by remember { mutableStateOf(false) }

    val deleteText = when (isSyncedPrescription) {
        true -> stringResource(R.string.pres_detail_delete)
        false -> stringResource(R.string.scanned_prescription_delete)
    }

    Button(
        onClick = { showDeletePrescriptionDialog = true },
        modifier = Modifier
            .padding(
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
                top = PaddingDefaults.Medium * 2,
                bottom = PaddingDefaults.Medium
            )
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = AppTheme.colors.red600,
            contentColor = AppTheme.colors.neutral000
        )
    ) {
        Text(
            deleteText.uppercase(Locale.getDefault()),
            modifier = Modifier.padding(
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
                top = PaddingDefaults.Medium / 2,
                bottom = PaddingDefaults.Medium / 2
            )
        )
    }

    if (showDeletePrescriptionDialog) {
        val info = stringResource(R.string.pres_detail_delete_msg)
        val cancelText = stringResource(R.string.pres_detail_delete_no)
        val actionText = stringResource(R.string.pres_detail_delete_yes)

        CommonAlertDialog(
            header = null,
            info = info,
            cancelText = cancelText,
            actionText = actionText,
            onCancel = {
                showDeletePrescriptionDialog = false
            },
            onClickAction = {
                onClickDelete()
                showDeletePrescriptionDialog = false
            }
        )
    }
}

@Composable
private fun FullDetailSecondHeader(
    prescriptionDetail: UIPrescriptionDetailSynced,
    onClickRedeem: () -> Unit
) {
    val dtFormatter =
        remember(LocalConfiguration.current) { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

    val text =
        if (prescriptionDetail.medicationDispense != null) {
            stringResource(
                id = R.string.pres_detail_medication_redeemed_on,
                prescriptionDetail.medicationDispense.whenHandedOver.format(dtFormatter)
            )
        } else if (prescriptionDetail.taskStatus == TaskStatus.InProgress) {
            stringResource(
                id = R.string.pres_detail_medication_in_progress,
            )
        } else {
            prescriptionDetail.redeemUntil?.let { expiryDate ->
                prescriptionDetail.acceptUntil?.let { acceptDate ->
                    expiryOrAcceptString(
                        expiryDate = expiryDate,
                        acceptDate = acceptDate,
                        nowInEpochDays = LocalDate.now().toEpochDay()
                    )
                }
            }
        } ?: ""
    Text(
        text = text,
        style = AppTheme.typography.body2l,
        modifier = Modifier.padding(
            start = PaddingDefaults.Medium,
            end = PaddingDefaults.Medium
        )
    )
    Spacer4()

    var redeemable by remember { mutableStateOf(false) }
    prescriptionDetail.redeemUntil.let {
        if (
            it != null && it.toEpochDay() >= LocalDate.now().toEpochDay() &&
            prescriptionDetail.accessCode != null &&
            prescriptionDetail.taskStatus == TaskStatus.Ready
        ) {
            redeemable = true
        }
    }

    if (prescriptionDetail.redeemedOn == null && redeemable) {
        Button(
            onClick = { onClickRedeem() },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 46.dp)
                .padding(
                    start = PaddingDefaults.Medium,
                    end = PaddingDefaults.Medium,
                    top = 24.dp,
                    bottom = PaddingDefaults.Medium
                )
        ) {
            Text(
                stringResource(R.string.pres_detail_medication_redeem_button_text).uppercase(Locale.getDefault()),
                modifier = Modifier.padding(
                    horizontal = PaddingDefaults.Medium,
                    vertical = PaddingDefaults.Small
                )
            )
        }
    }
}

@Composable
private fun LowDetailRedeemHeader(
    prescriptionDetail: UIPrescriptionDetailScanned,
    onSwitchRedeemed: (redeem: Boolean, all: Boolean, protocolText: String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        RedeemedButton(
            prescriptionDetail.redeemedOn != null,
            prescriptionDetail.unRedeemMorePossible,
            onSwitchRedeemed
        )
    }

    // mark as redeemed information hint
    if (prescriptionDetail.redeemedOn == null) {
        Spacer16()
        HintCard(
            image = {
                HintSmallImage(
                    painterResource(R.drawable.pharmacist_hint),
                    innerPadding = it
                )
            },
            title = { Text(stringResource(R.string.scanned_prescription_detail_hint_header)) },
            body = { Text(stringResource(R.string.scanned_prescription_detail_hint_info)) },
            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
        )
    }
}

@Composable
private fun RedeemedButton(
    redeemed: Boolean,
    unRedeemMorePossible: Boolean,
    onSwitchRedeemed: (redeem: Boolean, all: Boolean, protocolText: String) -> Unit
) {

    val context = LocalContext.current
    var currentRedeemed by remember { mutableStateOf(redeemed) }
    var infoText by remember { mutableStateOf("") }

    val redeemedInfo = stringResource(R.string.prescription_detail_redeemed)
    val unRedeemedInfo = stringResource(R.string.prescription_detail_un_redeemed)

    DisposableEffect(currentRedeemed) {
        infoText = if (currentRedeemed) {
            unRedeemedInfo
        } else {
            redeemedInfo
        }
        onDispose { }
    }

    var showUnRedeemDialog by remember { mutableStateOf(false) }
    val redeemProtocolText = stringResource(R.string.redeem_protocol_text)
    val unRedeemProtocolText = stringResource(R.string.un_redeem_protocol_text)

    if (showUnRedeemDialog) {
        UnRedeemPrescriptionDialog(
            onSwitchRedeemed = { redeem, all, protocolText ->
                onSwitchRedeemed(redeem, all, protocolText)
                showUnRedeemDialog = false
                currentRedeemed = !currentRedeemed
                createToastShort(context, infoText)
            },
        )
    }

    val buttonColors = if (currentRedeemed) {
        ButtonDefaults.buttonColors(
            backgroundColor = AppTheme.colors.neutral050,
            contentColor = AppTheme.colors.primary700
        )
    } else {
        ButtonDefaults.buttonColors(
            backgroundColor = AppTheme.colors.primary600,
            contentColor = AppTheme.colors.neutral000
        )
    }

    val buttonText = if (currentRedeemed) {
        stringResource(R.string.scanned_prescription_details_mark_as_unredeemed)
    } else {
        stringResource(R.string.scanned_prescription_details_mark_as_redeemed)
    }

    val protocolText = if (currentRedeemed) {
        unRedeemProtocolText
    } else {
        redeemProtocolText
    }

    Button(
        onClick = {
            if (currentRedeemed && unRedeemMorePossible) {
                showUnRedeemDialog = true
            } else {
                onSwitchRedeemed(!currentRedeemed, false, protocolText)
                currentRedeemed = !currentRedeemed
                createToastShort(context, infoText)
            }
        },
        colors = buttonColors,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 46.dp)
    ) {
        Text(
            buttonText.uppercase(Locale.getDefault())
        )
    }
}

@Composable
private fun UnRedeemPrescriptionDialog(
    onSwitchRedeemed: (redeem: Boolean, all: Boolean, protocol: String) -> Unit,
) {
    val unRedeemProtocolText = stringResource(R.string.un_redeem_protocol_text)

    AlertDialog(
        onDismissRequest = { onSwitchRedeemed(false, false, unRedeemProtocolText) },
        text = {
            Text(
                stringResource(R.string.pres_detail_un_redeem_msg)
            )
        },
        buttons = {
            TextButton(
                onClick = {
                    onSwitchRedeemed(
                        false,
                        false,
                        unRedeemProtocolText
                    )
                }
            ) {
                Text(stringResource(R.string.pres_detail_un_redeem_selected).uppercase(Locale.getDefault()))
            }
            TextButton(
                onClick = {
                    onSwitchRedeemed(
                        false,
                        true,
                        unRedeemProtocolText
                    )
                }
            ) {
                Text(stringResource(R.string.pres_detail_un_redeem_all).uppercase(Locale.getDefault()))
            }
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}

@Composable
private fun MedicationInformation(
    state: UIPrescriptionDetailSynced,
    isSubstituted: Boolean
) {

    val medicationType = if (isSubstituted) {
        state.medicationDispense?.type?.let { codeToDosageFormMapping[it] }
            ?.let { stringResource(it) } ?: MISSING_VALUE
    } else {
        state.medication.type?.let { stringResource(it) } ?: MISSING_VALUE
    }

    val uniqueIdentifier = if (isSubstituted) {
        state.medicationDispense?.uniqueIdentifier ?: MISSING_VALUE
    } else {
        state.medication.uniqueIdentifier ?: MISSING_VALUE
    }

    SubHeader(
        text = stringResource(id = R.string.pres_detail_medication_header)
    )

    Label(
        text = medicationType,
        label = stringResource(id = R.string.pres_detail_medication_label_dosage_form)
    )

    Label(
        text = if (state.medication.normSize != null) {
            if (state.medication.normSize.text != null) {
                "${state.medication.normSize.code} - ${stringResource(state.medication.normSize.text)}"
            } else {
                state.medication.normSize.code
            }
        } else {
            MISSING_VALUE
        },
        label = stringResource(id = R.string.pres_detail_medication_label_normsize)
    )

    Label(
        text = uniqueIdentifier,
        label = stringResource(id = R.string.pres_detail_medication_label_id)
    )
}

@Composable
private fun WasSubstitutedHint() {

    HintCard(
        modifier = Modifier.padding(PaddingDefaults.Medium),
        properties = HintCardDefaults.properties(
            backgroundColor = AppTheme.colors.red100,
            contentColor = AppTheme.colors.neutral999,
            border = BorderStroke(0.0.dp, AppTheme.colors.neutral300),
            elevation = 0.dp
        ),
        image = {
            HintSmallImage(
                painterResource(R.drawable.medical_hand_out_circle_red),
                innerPadding = it
            )
        },
        title = { Text(stringResource(R.string.pres_detail_substituted_header)) },
        body = { Text(stringResource(R.string.pres_detail_substituted_info)) }
    )
}

@Composable
private fun ColumnScope.DosageInformation(
    state: UIPrescriptionDetailSynced,
    isSubstituted: Boolean
) {
    val infoText = if (isSubstituted) {
        state.medicationDispense?.dosageInstruction
            ?: stringResource(id = R.string.pres_detail_dosage_default_info)
    } else {
        state.medicationRequest.dosageInstruction
            ?: stringResource(id = R.string.pres_detail_dosage_default_info)
    }

    SubHeader(
        text = stringResource(id = R.string.pres_detail_dosage_header)
    )
    HintCard(
        modifier = Modifier.padding(start = PaddingDefaults.Medium, end = PaddingDefaults.Medium),
        image = { HintSmallImage(painterResource(R.drawable.doctor_circle), innerPadding = it) },
        title = null,
        body = { Text(infoText) },
    )
}

@Composable
fun ColumnScope.HealthPortalLink() {
    Spacer16()
    Text(
        modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
        text = stringResource(id = R.string.pres_detail_health_portal_description),
        style = MaterialTheme.typography.body2,
        color = AppTheme.typographyColors.body2l
    )
    Spacer8()
    val linkInfo = stringResource(id = R.string.pres_detail_health_portal_description_url_info)
    val link = stringResource(id = R.string.pres_detail_health_portal_description_url)
    val uriHandler = LocalUriHandler.current
    val annotatedLink =
        annotatedLinkStringLight(link, linkInfo)
    ClickableText(
        text = annotatedLink,
        onClick = {
            annotatedLink
                .getStringAnnotations("URL", it, it)
                .firstOrNull()?.let { stringAnnotation ->
                    uriHandler.openUri(stringAnnotation.item)
                }
        },
        modifier = Modifier
            .align(Alignment.End)
            .padding(end = PaddingDefaults.Medium)
    )
}

@Composable
private fun PatientInformation(
    patient: PatientDetail,
    insurance: InsuranceCompanyDetail
) {
    val dtFormatter =
        remember(LocalConfiguration.current) { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

    SubHeader(
        text = stringResource(id = R.string.pres_detail_patient_header)
    )

    Label(
        text = patient.name ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_patient_label_name)
    )

    Label(
        text = patient.address ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_patient_label_address)
    )

    Label(
        text = patient.birthdate?.format(dtFormatter) ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_patient_label_birthdate)
    )

    Label(
        text = insurance.name ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_patient_label_insurance)
    )

    Label(
        text = insurance.status?.let { stringResource(it) } ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_patient_label_member_status)
    )

    Label(
        text = patient.insuranceIdentifier ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_patient_label_insurance_id)
    )
}

@Composable
private fun PractitionerInformation(
    practitioner: PractitionerDetail
) {
    SubHeader(
        text = stringResource(id = R.string.pres_detail_practitioner_header)
    )

    Label(
        text = practitioner.name ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_practitioner_label_name)
    )

    Label(
        text = practitioner.qualification ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_practitioner_label_qualification)
    )

    Label(
        text = practitioner.practitionerIdentifier ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_practitioner_label_id)
    )
}

@Composable
private fun OrganizationInformation(
    organization: OrganizationDetail
) {
    SubHeader(
        text = stringResource(id = R.string.pres_detail_organization_header)
    )

    Label(
        text = organization.name ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_organization_label_name)
    )

    Label(
        text = organization.address ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_organization_label_address)
    )

    Label(
        text = organization.uniqueIdentifier ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_organization_label_id)
    )

    Label(
        text = organization.phone ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_organization_label_telephone)
    )

    Label(
        text = organization.mail ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_organization_label_email)
    )
}

@Composable
private fun AccidentInformation(
    medicationRequest: MedicationRequestDetail
) {
    val dtFormatter =
        remember(LocalConfiguration.current) { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

    SubHeader(
        text = stringResource(id = R.string.pres_detail_accident_header)
    )

    Label(
        text = medicationRequest.dateOfAccident?.format(dtFormatter)
            ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_accident_label_date)
    )

    Label(
        text = medicationRequest.location ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_accident_label_location)
    )
}

@Composable
private fun ProtocolScanned(
    uiPrescriptionDetail: UIPrescriptionDetailScanned,
    lowDetailRedeemEvents: List<LowDetailEventSimple>
) {
    val firstScan = stringResource(
        R.string.scanned_prescription_detail_protocol_scanned_at,
        uiPrescriptionDetail.formattedScannedInfo(stringResource(R.string.at))
    )

    SubHeader(
        text = stringResource(id = R.string.scanned_prescription_detail_protocol_header)
    )

    Label(
        text = firstScan,
        label = stringResource(id = R.string.scanned_prescription_detail_protocol_scanned_label)
    )

    lowDetailRedeemEvents.map {
        Label(
            text = phrasedDateString(it.timestamp.toLocalDateTime()),
            label = it.text
        )
    }
}

@Composable
private fun TechnicalPrescriptionInformation(accessCode: String?, taskId: String) {
    SubHeader(stringResource(R.string.pres_detail_technical_information))

    if (accessCode != null) {
        Label(
            text = accessCode,
            label = stringResource(id = R.string.access_code)
        )
    }

    Label(
        text = taskId,
        label = stringResource(id = R.string.task_id)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Label(
    text: String,
    label: String
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    clipboardManager.setText(AnnotatedString(text))
                    Toast
                        .makeText(context, "$label $text", Toast.LENGTH_SHORT)
                        .show()
                }
            )
            .padding(PaddingDefaults.Medium)
            .fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.body1
        )
        Spacer4()
        Text(
            text = label,
            style = MaterialTheme.typography.body2,
            color = AppTheme.typographyColors.body2l
        )
    }
}

@Composable
private fun Header(
    text: String
) = Text(
    text = text,
    style = MaterialTheme.typography.h6,
    fontWeight = FontWeight(500),
    modifier = Modifier.padding(
        start = PaddingDefaults.Medium,
        end = PaddingDefaults.Medium,
        top = PaddingDefaults.Medium * 1.5f
    )
)

@Composable
private fun SubHeader(
    text: String
) =
    Text(
        text = text,
        style = MaterialTheme.typography.subtitle1,
        fontWeight = FontWeight(500),
        modifier = Modifier.padding(
            top = 40.dp,
            end = PaddingDefaults.Medium,
            start = PaddingDefaults.Medium,
            bottom = PaddingDefaults.Medium
        )
    )

@Composable
private fun EmergencyServiceCard() {
    Card(
        modifier = Modifier
            .padding(
                start = PaddingDefaults.Medium,
                top = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium
            )
            .fillMaxWidth()
    ) {
        Row {
            Image(
                painterResource(R.drawable.pharmacist),
                null,
                alignment = Alignment.BottomStart
            )
            Column {
                Text(
                    stringResource(R.string.pres_detail_noctu_header),
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    stringResource(R.string.pres_detail_noctu_info),
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}

@Composable
fun SubstitutionAllowed() {
    HintCard(
        modifier = Modifier.padding(PaddingDefaults.Medium),
        properties = HintCardDefaults.properties(
            backgroundColor = AppTheme.colors.primary100,
            border = BorderStroke(0.0.dp, AppTheme.colors.neutral300),
            elevation = 0.dp
        ),
        image = {
            HintSmallImage(
                painterResource(R.drawable.pharmacist_circle),
                innerPadding = it
            )
        },
        title = { Text(stringResource(R.string.pres_detail_aut_idem_header)) },
        body = { Text(stringResource(R.string.pres_detail_aut_idem_info)) },
        action = {
            HintTextLearnMoreButton()
        }
    )
}
