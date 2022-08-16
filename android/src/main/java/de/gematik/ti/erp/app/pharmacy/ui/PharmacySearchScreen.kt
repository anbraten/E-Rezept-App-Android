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

package de.gematik.ti.erp.app.pharmacy.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.google.accompanist.flowlayout.FlowRow
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.fhir.model.LocalPharmacyService
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.Chip
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.time.OffsetDateTime

private const val OneKilometerInMeter = 1000

@Composable
private fun PharmacySearchErrorHint(
    title: String,
    subtitle: String,
    action: String? = null,
    onClickAction: (() -> Unit)? = null,
    modifier: Modifier
) {
    Box(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(PaddingDefaults.Medium),
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = AppTheme.typography.subtitle1,
                textAlign = TextAlign.Center
            )
            Text(
                subtitle,
                style = AppTheme.typography.body2l,
                textAlign = TextAlign.Center
            )
            if (action != null && onClickAction != null) {
                TextButton(onClick = onClickAction) {
                    Text(action)
                }
            }
        }
    }
}

@Composable
fun EnableLocationDialog(
    onCancel: () -> Unit,
    onAccept: () -> Unit
) {
    CommonAlertDialog(
        header = stringResource(R.string.search_pharmacies_location_na_header),
        info = stringResource(R.string.search_enable_location_hint_info),
        cancelText = stringResource(R.string.search_pharmacies_location_na_cancel),
        actionText = stringResource(R.string.search_enable_location_hint_enable),
        onCancel = onCancel,
        onClickAction = onAccept
    )
}

@Composable
private fun PharmacySearchInputfield(
    onBack: () -> Unit,
    isLoading: Boolean,
    searchValue: String,
    onSearchChange: (String) -> Unit,
    onSearch: (String) -> Unit
) {
    var isLoadingStable by remember { mutableStateOf(isLoading) }

    LaunchedEffect(isLoading) {
        delay(timeMillis = 330)
        isLoadingStable = isLoading
    }

    TextField(
        value = searchValue,
        onValueChange = onSearchChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            autoCorrect = true,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions {
            onSearch(searchValue)
        },
        visualTransformation = VisualTransformation.None,
        trailingIcon = {
            Crossfade(isLoadingStable, animationSpec = tween(durationMillis = 550)) {
                if (it) {
                    Box(Modifier.size(48.dp)) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).align(Alignment.Center),
                            strokeWidth = 2.dp
                        )
                    }
                } else {
                    IconButton(
                        onClick = { onSearchChange("") }
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = null
                        )
                    }
                }
            }
        },
        leadingIcon = {
            IconButton(
                onClick = { onBack() }
            ) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = null
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        textStyle = AppTheme.typography.body1,
        colors = TextFieldDefaults.textFieldColors(
            textColor = AppTheme.colors.neutral900,
            leadingIconColor = AppTheme.colors.neutral600,
            trailingIconColor = AppTheme.colors.neutral600,
            backgroundColor = AppTheme.colors.neutral050,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
private fun FilterSection(
    filter: PharmacyUseCaseData.Filter,
    onClickChip: (PharmacyUseCaseData.Filter) -> Unit,
    onClickFilter: () -> Unit
) {
    val rowState = rememberLazyListState()
    Row(modifier = Modifier.fillMaxWidth()) {
        SpacerMedium()
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    onClickFilter()
                }
                .background(color = AppTheme.colors.neutral100, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = PaddingDefaults.Small, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Tune, null, Modifier.size(16.dp), tint = AppTheme.colors.primary600)
            SpacerSmall()
            Text(
                stringResource(R.string.search_pharmacies_filter),
                style = AppTheme.typography.subtitle2,
                color = AppTheme.colors.primary600
            )
        }
        if (filter.isAnySet()) {
            SpacerSmall()
            LazyRow(
                state = rowState,
                horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (filter.nearBy) {
                    item {
                        Chip(
                            stringResource(R.string.search_pharmacies_filter_nearby),
                            closable = true,
                            checked = false
                        ) {
                            onClickChip(filter.copy(nearBy = false))
                        }
                    }
                }
                if (filter.openNow) {
                    item {
                        Chip(
                            stringResource(R.string.search_pharmacies_filter_open_now),
                            closable = true,
                            checked = false
                        ) {
                            onClickChip(filter.copy(openNow = false))
                        }
                    }
                }
                if (filter.ready) {
                    item {
                        Chip(
                            stringResource(R.string.search_pharmacies_filter_e_prescription_ready),
                            closable = true,
                            checked = false
                        ) {
                            onClickChip(filter.copy(ready = false))
                        }
                    }
                }
                if (filter.deliveryService) {
                    item {
                        Chip(
                            stringResource(R.string.search_pharmacies_filter_delivery_service),
                            closable = true,
                            checked = false
                        ) {
                            onClickChip(filter.copy(deliveryService = false))
                        }
                    }
                }
                if (filter.onlineService) {
                    item {
                        Chip(
                            stringResource(R.string.search_pharmacies_filter_online_service),
                            closable = true,
                            checked = false
                        ) {
                            onClickChip(filter.copy(onlineService = false))
                        }
                    }
                }
                item {
                    SpacerSmall()
                }
            }
        }
    }
}

@Composable
fun FilterBottomSheet(
    modifier: Modifier,
    extraContent: @Composable () -> Unit = {},
    filter: PharmacyUseCaseData.Filter,
    onClickChip: (PharmacyUseCaseData.Filter) -> Unit,
    onClickClose: () -> Unit
) {
    Column(
        modifier.padding(PaddingDefaults.Medium)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.search_pharmacies_filter_header),
                style = AppTheme.typography.h6
            )
            IconButton(
                modifier = Modifier
                    .background(AppTheme.colors.neutral100, CircleShape),
                onClick = onClickClose
            ) {
                Icon(
                    Icons.Rounded.Close,
                    null
                )
            }
        }
        SpacerMedium()
        Column(modifier = Modifier.verticalScroll(rememberScrollState(), true)) {
            FlowRow(
                mainAxisSpacing = PaddingDefaults.Small,
                crossAxisSpacing = PaddingDefaults.Small
            ) {
                Chip(
                    stringResource(R.string.search_pharmacies_filter_nearby),
                    closable = false,
                    checked = filter.nearBy
                ) {
                    onClickChip(
                        filter.copy(
                            nearBy = it
                        )
                    )
                }
                Chip(
                    stringResource(R.string.search_pharmacies_filter_open_now),
                    closable = false,
                    checked = filter.openNow
                ) {
                    onClickChip(
                        filter.copy(
                            openNow = it
                        )
                    )
                }
                Chip(
                    stringResource(R.string.search_pharmacies_filter_e_prescription_ready),
                    closable = false,
                    checked = filter.ready
                ) {
                    onClickChip(
                        filter.copy(
                            ready = it
                        )
                    )
                }
                Chip(
                    stringResource(R.string.search_pharmacies_filter_delivery_service),
                    closable = false,
                    checked = filter.deliveryService
                ) {
                    onClickChip(
                        filter.copy(
                            nearBy = if (it) true else filter.nearBy,
                            deliveryService = it
                        )
                    )
                }
                Chip(
                    stringResource(R.string.search_pharmacies_filter_online_service),
                    closable = false,
                    checked = filter.onlineService
                ) {
                    onClickChip(
                        filter.copy(
                            onlineService = it
                        )
                    )
                }
            }

            extraContent()
        }
    }
}

@Composable
private fun PharmacyResultCard(
    modifier: Modifier,
    pharmacy: PharmacyUseCaseData.Pharmacy,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val distanceTxt = pharmacy.distance?.let { distance ->
            formattedDistance(distance)
        }

        PharmacyImagePlaceholder(Modifier)
        SpacerMedium()
        Column(modifier = Modifier.weight(1f)) {
            Text(
                pharmacy.name,
                style = AppTheme.typography.subtitle1
            )

            Text(
                pharmacy.removeLineBreaksFromAddress(),
                style = AppTheme.typography.body2l,
                modifier = Modifier,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )

            val pharmacyLocalServices = pharmacy.provides.find { it is LocalPharmacyService } as LocalPharmacyService
            val now = OffsetDateTime.now()

            if (pharmacyLocalServices.isOpenAt(now)) {
                val text = if (pharmacyLocalServices.isAllDayOpen(now.dayOfWeek)) {
                    stringResource(R.string.search_pharmacy_continuous_open)
                } else {
                    stringResource(
                        R.string.search_pharmacy_open_until,
                        requireNotNull(pharmacyLocalServices.openUntil(now)).toString()
                    )
                }
                Text(
                    text,
                    style = AppTheme.typography.subtitle2l,
                    color = AppTheme.colors.green600
                )
            } else {
                val text =
                    pharmacyLocalServices.opensAt(now)?.let {
                        stringResource(
                            R.string.search_pharmacy_opens_at,
                            it.toString()
                        )
                    }
                if (text != null) {
                    Text(
                        text,
                        style = AppTheme.typography.subtitle2l,
                        color = AppTheme.colors.yellow600
                    )
                }
            }
        }

        SpacerMedium()

        if (distanceTxt != null) {
            Text(
                distanceTxt,
                style = AppTheme.typography.body2l,
                modifier = Modifier
                    .align(Alignment.CenterVertically),
                textAlign = TextAlign.End
            )
        }
        Icon(
            Icons.Rounded.KeyboardArrowRight,
            null,
            tint = AppTheme.colors.neutral400,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterVertically)
        )
    }
}

private fun formattedDistance(distanceInMeters: Double): String {
    val f = DecimalFormat()
    return if (distanceInMeters < OneKilometerInMeter) {
        f.maximumFractionDigits = 0
        f.format(distanceInMeters).toString() + " m"
    } else {
        f.maximumFractionDigits = 1
        f.format(distanceInMeters / OneKilometerInMeter).toString() + " km"
    }
}

@Composable
private fun ErrorRetryHandler(
    searchPagingItems: LazyPagingItems<PharmacySearchUi>,
    scaffoldState: ScaffoldState
) {
    val errorTitle = stringResource(R.string.search_pharmacy_error_title)
    val errorAction = stringResource(R.string.search_pharmacy_error_action)

    LaunchedEffect(searchPagingItems.loadState) {
        searchPagingItems.loadState.let {
            val anyErr = it.append is LoadState.Error ||
                it.prepend is LoadState.Error ||
                it.refresh is LoadState.Error
            if (anyErr && searchPagingItems.itemCount > 1) {
                val result =
                    scaffoldState.snackbarHostState.showSnackbar(
                        errorTitle,
                        errorAction,
                        duration = SnackbarDuration.Short
                    )
                if (result == SnackbarResult.ActionPerformed) {
                    searchPagingItems.retry()
                }
            }
        }
    }
}

@Suppress("LongMethod")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PharmacySearchResultScreen(
    pharmacySearchController: PharmacySearchController,
    onSelectPharmacy: (PharmacyUseCaseData.Pharmacy) -> Unit,
    onBack: () -> Unit
) {
    var showEnableLocationDialog by remember { mutableStateOf(false) }

    val searchPagingItems = pharmacySearchController.pharmacySearchFlow.collectAsLazyPagingItems()

    val scaffoldState = rememberScaffoldState()

    var searchName by remember(pharmacySearchController.searchState.name) {
        mutableStateOf(pharmacySearchController.searchState.name)
    }
    var searchFilter by remember(pharmacySearchController.searchState.filter) {
        mutableStateOf(pharmacySearchController.searchState.filter)
    }

    ErrorRetryHandler(
        searchPagingItems,
        scaffoldState
    )

    val scope = rememberCoroutineScope()

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            scope.launch {
                pharmacySearchController.search(
                    name = searchName,
                    filter = searchFilter.copy(nearBy = permissions.values.any { it })
                )
            }
        }

    if (showEnableLocationDialog) {
        EnableLocationDialog(
            onCancel = {
                searchFilter = searchFilter.copy(nearBy = false)
                showEnableLocationDialog = false
            },
            onAccept = {
                locationPermissionLauncher.launch(locationPermissions)
                showEnableLocationDialog = false
            }
        )
    }

    val loadState = searchPagingItems.loadState
    val isLoading by derivedStateOf {
        pharmacySearchController.isLoading || listOf(loadState.prepend, loadState.append, loadState.refresh)
            .any {
                when (it) {
                    is LoadState.NotLoading -> false // initial ui only loading indicator
                    is LoadState.Loading -> true
                    else -> false
                }
            }
    }

    val modal = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    val focusManager = LocalFocusManager.current

    ModalBottomSheetLayout(
        modifier = Modifier.fillMaxSize(),
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContent = {
            FilterBottomSheet(
                modifier = Modifier.navigationBarsPadding(),
                filter = searchFilter,
                onClickChip = {
                    focusManager.clearFocus()
                    scope.launch {
                        searchFilter = it
                        when (pharmacySearchController.search(name = searchName, filter = it)) {
                            PharmacySearchController.SearchQueryResult.Send -> {}
                            PharmacySearchController.SearchQueryResult.NoLocationPermission -> {
                                showEnableLocationDialog = true
                            }
                            PharmacySearchController.SearchQueryResult.NoLocationFound -> {
                                searchFilter = searchFilter.copy(nearBy = false)
                            }
                        }
                    }
                },
                onClickClose = { scope.launch { modal.hide() } }
            )
        },
        sheetState = modal
    ) {
        Column(Modifier.systemBarsPadding()) {
            Column {
                SpacerMedium()
                PharmacySearchInputfield(
                    onBack = onBack,
                    isLoading = isLoading,
                    searchValue = searchName,
                    onSearchChange = { searchName = it },
                    onSearch = {
                        focusManager.clearFocus()
                        scope.launch {
                            when (pharmacySearchController.search(name = it, filter = searchFilter)) {
                                PharmacySearchController.SearchQueryResult.Send -> {}
                                PharmacySearchController.SearchQueryResult.NoLocationPermission -> {
                                    showEnableLocationDialog = true
                                }
                                PharmacySearchController.SearchQueryResult.NoLocationFound -> {
                                    searchFilter = searchFilter.copy(nearBy = false)
                                }
                            }
                        }
                    }
                )
                SpacerSmall()

                FilterSection(
                    filter = searchFilter,
                    onClickChip = {
                        focusManager.clearFocus()
                        scope.launch {
                            pharmacySearchController.search(name = searchName, filter = it)
                        }
                    },
                    onClickFilter = {
                        focusManager.clearFocus()
                        scope.launch { modal.show() }
                    }
                )

                SpacerSmall()
            }

            SearchResultContent(
                searchPagingItems = searchPagingItems,
                onSelectPharmacy = onSelectPharmacy
            )
        }
    }
}

@Composable
private fun SearchResultContent(
    searchPagingItems: LazyPagingItems<PharmacySearchUi>,
    onSelectPharmacy: (PharmacyUseCaseData.Pharmacy) -> Unit
) {
    val errorTitle = stringResource(R.string.search_pharmacy_error_title)
    val errorSubtitle = stringResource(R.string.search_pharmacy_error_subtitle)
    val errorAction = stringResource(R.string.search_pharmacy_error_action)

    val itemPaddingModifier = Modifier
        .fillMaxWidth()
        .padding(PaddingDefaults.Medium)
    val loadState = searchPagingItems.loadState

    val showNothingFound by derivedStateOf {
        listOf(loadState.prepend, loadState.append)
            .all {
                when (it) {
                    is LoadState.NotLoading ->
                        it.endOfPaginationReached && searchPagingItems.itemCount == 0
                    else -> false
                }
            } && loadState.refresh is LoadState.NotLoading
    }

    val showError by derivedStateOf { searchPagingItems.itemCount <= 1 && loadState.refresh is LoadState.Error }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        state = rememberLazyListState(),
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
            .asPaddingValues()
    ) {
        if (showNothingFound) {
            item {
                PharmacySearchErrorHint(
                    title = stringResource(R.string.search_pharmacy_nothing_found_header),
                    subtitle = stringResource(R.string.search_pharmacy_nothing_found_info),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillParentMaxHeight()
                )
            }
        }
        if (showError) {
            item {
                PharmacySearchErrorHint(
                    title = errorTitle,
                    subtitle = errorSubtitle,
                    action = errorAction,
                    onClickAction = { searchPagingItems.retry() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillParentMaxHeight()
                )
            }
        }
        if (loadState.prepend is LoadState.Error) {
            item {
                PharmacySearchErrorHint(
                    title = errorTitle,
                    subtitle = errorSubtitle,
                    action = errorAction,
                    onClickAction = { searchPagingItems.retry() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(PaddingDefaults.Medium)
                )
            }
        }
        itemsIndexed(searchPagingItems) { index, item ->
            when (item) {
                is PharmacySearchUi.Pharmacy -> {
                    Column {
                        PharmacyResultCard(
                            modifier = itemPaddingModifier,
                            pharmacy = item.pharmacy
                        ) {
                            onSelectPharmacy(item.pharmacy)
                        }
                        if (index < searchPagingItems.itemCount - 1) {
                            Divider(startIndent = PaddingDefaults.Medium)
                        }
                    }
                }
                null -> {}
            }
        }
        if (loadState.append is LoadState.Error) {
            item {
                PharmacySearchErrorHint(
                    title = errorTitle,
                    subtitle = errorSubtitle,
                    action = errorAction,
                    onClickAction = { searchPagingItems.retry() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(PaddingDefaults.Medium)
                )
            }
        }
    }
}
