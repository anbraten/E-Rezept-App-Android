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

package de.gematik.ti.erp.app.cardwall.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsPadding
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.cardwall.ui.model.CardWallData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.annotatedLinkString
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import kotlinx.coroutines.launch

@Composable
fun CardWallTroubleshootingPageA(
    viewModel: CardWallViewModel,
    authenticationMethod: CardWallData.AuthenticationMethod,
    cardAccessNumber: String,
    personalIdentificationNumber: String,
    onFinal: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onRetryCan: () -> Unit,
    onRetryPin: () -> Unit,
) {
    val dialogState = rememberCardWallAuthenticationDialogState()

    CardWallAuthenticationDialog(
        dialogState = dialogState,
        viewModel = viewModel,
        authenticationMethod = authenticationMethod,
        cardAccessNumber = cardAccessNumber,
        personalIdentificationNumber = personalIdentificationNumber,
        onFinal = onFinal,
        onRetryCan = onRetryCan,
        onRetryPin = onRetryPin
    )

    val coroutineScope = rememberCoroutineScope()

    TroubleshootingScaffold(
        title = stringResource(R.string.cdw_troubleshooting_page_a_title),
        onBack = onBack,
        bottomBarButton = { NextTipButton(onClick = onNext) }
    ) {
        Column {
            Tip(stringResource(R.string.cdw_troubleshooting_page_a_tip1))
            SpacerMedium()
            Tip(stringResource(R.string.cdw_troubleshooting_page_a_tip2))
            SpacerMedium()
            Tip(stringResource(R.string.cdw_troubleshooting_page_a_tip3))
            SpacerLarge()
            TryMeButton {
                coroutineScope.launch { dialogState.show() }
            }
        }
    }
}

@Composable
fun CardWallTroubleshootingPageB(
    viewModel: CardWallViewModel,
    authenticationMethod: CardWallData.AuthenticationMethod,
    cardAccessNumber: String,
    personalIdentificationNumber: String,
    onFinal: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onRetryCan: () -> Unit,
    onRetryPin: () -> Unit,
) {
    val dialogState = rememberCardWallAuthenticationDialogState()

    CardWallAuthenticationDialog(
        dialogState = dialogState,
        viewModel = viewModel,
        authenticationMethod = authenticationMethod,
        cardAccessNumber = cardAccessNumber,
        personalIdentificationNumber = personalIdentificationNumber,
        onFinal = onFinal,
        onRetryCan = onRetryCan,
        onRetryPin = onRetryPin
    )

    val coroutineScope = rememberCoroutineScope()

    TroubleshootingScaffold(
        title = stringResource(R.string.cdw_troubleshooting_page_b_title),
        onBack = onBack,
        bottomBarButton = { NextTipButton(onClick = onNext) }
    ) {
        Column {
            Tip(stringResource(R.string.cdw_troubleshooting_page_b_tip1))
            SpacerMedium()
            Tip(stringResource(R.string.cdw_troubleshooting_page_b_tip2))
            SpacerLarge()
            TryMeButton {
                coroutineScope.launch { dialogState.show() }
            }
        }
    }
}

@Composable
fun CardWallTroubleshootingPageC(
    viewModel: CardWallViewModel,
    authenticationMethod: CardWallData.AuthenticationMethod,
    cardAccessNumber: String,
    personalIdentificationNumber: String,
    onFinal: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onRetryCan: () -> Unit,
    onRetryPin: () -> Unit,
) {
    val dialogState = rememberCardWallAuthenticationDialogState()

    CardWallAuthenticationDialog(
        dialogState = dialogState,
        viewModel = viewModel,
        authenticationMethod = authenticationMethod,
        cardAccessNumber = cardAccessNumber,
        personalIdentificationNumber = personalIdentificationNumber,
        onFinal = onFinal,
        onRetryCan = onRetryCan,
        onRetryPin = onRetryPin
    )

    val coroutineScope = rememberCoroutineScope()

    TroubleshootingScaffold(
        title = stringResource(R.string.cdw_troubleshooting_page_c_title),
        onBack = onBack,
        bottomBarButton = { NextButton(onClick = onNext) }
    ) {
        Column {
            val uriHandler = LocalUriHandler.current

            val tip1 = annotatedStringResource(
                R.string.cdw_troubleshooting_page_c_tip1,
                annotatedLinkString(
                    stringResource(R.string.cdw_troubleshooting_page_c_tip1_samsung_url),
                    stringResource(R.string.cdw_troubleshooting_page_c_tip1_samsung)
                )
            )

            val tip2 = annotatedStringResource(
                R.string.cdw_troubleshooting_page_c_tip2,
                annotatedLinkString(
                    stringResource(R.string.cdw_troubleshooting_page_c_tip2_google_url),
                    stringResource(R.string.cdw_troubleshooting_page_c_tip2_google)
                )
            )

            Tip(tip1, onClickText = { tag, item ->
                when (tag) {
                    "URL" -> uriHandler.openUri(item)
                }
            })
            SpacerMedium()
            Tip(tip2, onClickText = { tag, item ->
                when (tag) {
                    "URL" -> uriHandler.openUri(item)
                }
            })
            SpacerLarge()
            TryMeButton {
                coroutineScope.launch { dialogState.show() }
            }
        }
    }
}

@Composable
fun CardWallTroubleshootingNoSuccessPage(
    onClickContactUs: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    TroubleshootingScaffold(
        title = stringResource(R.string.cdw_troubleshooting_no_success_title),
        onBack = onBack,
        bottomBarButton = { CloseButton(onClick = onNext) }
    ) {
        Column {
            Text(
                text = stringResource(R.string.cdw_troubleshooting_no_success_body),
                style = MaterialTheme.typography.body1
            )
            SpacerLarge()
            ContactUsButton(Modifier.align(Alignment.CenterHorizontally), onClick = onClickContactUs)
        }
    }
}

@Composable
private fun RowScope.NextTipButton(
    onClick: () -> Unit
) =
    SecondaryButton(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = PaddingDefaults.Medium, vertical = 12.dp)
            .weight(1f)
    ) {
        Icon(Icons.Outlined.Lightbulb, null)
        SpacerSmall()
        Text(stringResource(R.string.cdw_troubleshooting_next_tip_button))
    }

@Composable
private fun RowScope.NextButton(
    onClick: () -> Unit
) =
    SecondaryButton(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = PaddingDefaults.Medium, vertical = 12.dp)
            .weight(1f)
    ) {
        Text(stringResource(R.string.cdw_troubleshooting_next_button))
    }

@Composable
private fun RowScope.CloseButton(
    onClick: () -> Unit
) =
    PrimaryButton(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = PaddingDefaults.Medium, vertical = 12.dp)
            .weight(1f)
    ) {
        Text(stringResource(R.string.cdw_troubleshooting_close_button))
    }

@Composable
private fun ColumnScope.TryMeButton(
    onClick: () -> Unit
) =
    PrimaryButton(
        onClick = onClick,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    ) {
        Text(stringResource(R.string.cdw_troubleshooting_try_me_button))
    }

@Composable
private fun ContactUsButton(
    modifier: Modifier,
    onClick: () -> Unit
) =
    SecondaryButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(Icons.Rounded.Edit, null)
        SpacerSmall()
        Text(stringResource(R.string.cdw_troubleshooting_contact_us_button))
    }

@Composable
private fun Tip(
    text: String
) =
    Tip(AnnotatedString(text)) { _, _ -> }

@Composable
private fun Tip(
    text: AnnotatedString,
    onClickText: (tag: String, item: String) -> Unit
) {
    Row(Modifier.fillMaxWidth()) {
        Icon(Icons.Rounded.CheckCircle, null, tint = AppTheme.colors.green600)
        SpacerMedium()
        ClickableText(
            text = text,
            style = MaterialTheme.typography.body1,
            onClick = { offset ->
                text
                    .getStringAnnotations(offset, offset)
                    .firstOrNull()?.let {
                        onClickText(it.tag, it.item)
                    }
            }
        )
    }
}

@Composable
private fun TroubleshootingScaffold(
    title: String,
    onBack: () -> Unit,
    bottomBarButton: @Composable RowScope.() -> Unit,
    content: @Composable () -> Unit
) {
    val scrollState = rememberScrollState()

    AnimatedElevationScaffold(
        modifier = Modifier.testTag("cardWall/intro"),
        topBarTitle = stringResource(R.string.cdw_troubleshooting_title),
        topBarColor = MaterialTheme.colors.surface,
        elevated = scrollState.value > 0,
        navigationMode = NavigationBarMode.Back,
        bottomBar = {
            Surface(
                color = MaterialTheme.colors.surface,
                elevation = 4.dp
            ) {
                Row(Modifier.navigationBarsPadding()) {
                    bottomBarButton()
                }
            }
        },
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(it)
                .padding(PaddingDefaults.Medium)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.h6,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            SpacerLarge()
            content()
        }
    }
}
