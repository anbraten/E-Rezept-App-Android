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

package de.gematik.ti.erp.app.onboarding.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.PersonPin
import androidx.compose.material.icons.rounded.Star
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.Route
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.cardwall.ui.SecondaryButton
import de.gematik.ti.erp.app.mainscreen.ui.MainNavigationScreens
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.ui.AllowAnalyticsScreen
import de.gematik.ti.erp.app.settings.ui.AllowBiometryScreen
import de.gematik.ti.erp.app.settings.ui.SettingsViewModel
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.BottomAppBar
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.OutlinedDebugButton
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.createToastShort
import de.gematik.ti.erp.app.utils.compose.navigationModeState
import de.gematik.ti.erp.app.utils.compose.visualTestTag
import de.gematik.ti.erp.app.webview.URI_DATA_TERMS
import de.gematik.ti.erp.app.webview.URI_TERMS_OF_USE
import de.gematik.ti.erp.app.webview.WebViewScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

object OnboardingNavigationScreens {
    object Onboarding : Route("Onboarding")
    object Analytics : Route("Analytics")
    object TermsOfUse : Route("TermsOfUse")
    object DataProtection : Route("DataProtection")
    object Biometry : Route("Biometry")
}

private enum class OnboardingPages(val index: Int) {
    Welcome(index = 0),
    DataProtection(index = 1),
    SecureApp(index = 2),
    Analytics(index = 3);

    companion object {
        val MaxPage = OnboardingPages.values().size - 1

        fun pageOf(index: Int) =
            OnboardingPages.values().find {
                it.index == min(MaxPage, max(0, index))
            }!!
    }
}

@Composable
fun ReturningUserSecureAppOnboardingScreen(
    mainNavController: NavController,
    settingsViewModel: SettingsViewModel,
    secureMethod: OnboardingSecureAppMethod,
    onSecureMethodChange: (OnboardingSecureAppMethod) -> Unit
) {
    val enabled = when (secureMethod) {
        is OnboardingSecureAppMethod.DeviceSecurity -> true
        is OnboardingSecureAppMethod.Password -> (secureMethod as? OnboardingSecureAppMethod.Password)?.let {
            it.checkedPassword != null
        } ?: false

        else -> false
    }

    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        bottomBar = {
            BottomAppBar(backgroundColor = MaterialTheme.colors.surface) {
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    enabled = enabled,
                    onClick = {
                        coroutineScope.launch {
                            when (val sm = secureMethod) {
                                is OnboardingSecureAppMethod.DeviceSecurity ->
                                    settingsViewModel.onSelectDeviceSecurityAuthenticationMode()

                                is OnboardingSecureAppMethod.Password ->
                                    settingsViewModel.onSelectPasswordAsAuthenticationMode(
                                        requireNotNull(sm.checkedPassword)
                                    )

                                else -> error("Illegal state. Authentication must be set")
                            }
                            mainNavController.navigate(MainNavigationScreens.Prescriptions.path()) {
                                launchSingleTop = true
                                popUpTo(MainNavigationScreens.ReturningUserSecureAppOnboarding.path()) {
                                    inclusive = true
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(PaddingDefaults.Small)
                ) {
                    Text(stringResource(R.string.ok).uppercase(Locale.getDefault()))
                }
                SpacerMedium()
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            OnboardingSecureApp(
                secureMethod = secureMethod,
                onSecureMethodChange = onSecureMethodChange,
                onNextPage = {},
                onOpenBiometricScreen = { mainNavController.navigate(MainNavigationScreens.Biometry.path()) }
            )
        }
    }
}

@Composable
fun OnboardingScreen(
    mainNavController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    var allowAnalytics by rememberSaveable { mutableStateOf(false) }
    var secureMethod by rememberSaveable { mutableStateOf<OnboardingSecureAppMethod>(OnboardingSecureAppMethod.None) }

    val navigationMode by navController.navigationModeState(OnboardingNavigationScreens.Onboarding.route)

    NavHost(
        navController,
        startDestination = OnboardingNavigationScreens.Onboarding.route
    ) {
        composable(OnboardingNavigationScreens.Onboarding.route) {
            NavigationAnimation(mode = navigationMode) {
                OnboardingScreenWithScaffold(
                    navController,
                    secureMethod = secureMethod,
                    onSecureMethodChange = {
                        secureMethod = it
                    },
                    allowTracking = allowAnalytics,
                    onAllowTracking = {
                        allowAnalytics = it
                    },
                    onSaveNewUser = { allowTracking, defaultProfileName, secureMethod ->
                        coroutineScope.launch(Dispatchers.Main) {
                            settingsViewModel.onboardingSucceeded(
                                authenticationMode = when (secureMethod) {
                                    is OnboardingSecureAppMethod.DeviceSecurity ->
                                        SettingsData.AuthenticationMode.DeviceSecurity

                                    is OnboardingSecureAppMethod.Password ->
                                        SettingsData.AuthenticationMode.Password(
                                            password = requireNotNull(secureMethod.checkedPassword)
                                        )

                                    else -> error("Illegal state. Authentication must be set")
                                },
                                defaultProfileName = defaultProfileName,
                                allowTracking = allowTracking
                            )

                            mainNavController.navigate(MainNavigationScreens.Prescriptions.path()) {
                                launchSingleTop = true
                                popUpTo(MainNavigationScreens.Onboarding.path()) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                )
            }
        }
        composable(OnboardingNavigationScreens.Analytics.route) {
            NavigationAnimation(mode = navigationMode) {
                AllowAnalyticsScreen(
                    onBack = { navController.popBackStack() },
                    onAllowAnalytics = { allowAnalytics = it }
                )
            }
        }
        composable(OnboardingNavigationScreens.Biometry.route) {
            NavigationAnimation(mode = navigationMode) {
                AllowBiometryScreen(
                    onBack = { navController.popBackStack() },
                    onNext = { navController.popBackStack() },
                    onSecureMethodChange = { secureMethod = it }
                )
            }
        }
        composable(OnboardingNavigationScreens.TermsOfUse.route) {
            NavigationAnimation(mode = navigationMode) {
                WebViewScreen(
                    modifier = Modifier.testTag(TestTag.Onboarding.TermsOfUseScreen),
                    title = stringResource(R.string.onb_terms_of_use),
                    onBack = { navController.popBackStack() },
                    url = URI_TERMS_OF_USE
                )
            }
        }
        composable(OnboardingNavigationScreens.DataProtection.route) {
            NavigationAnimation(mode = navigationMode) {
                WebViewScreen(
                    modifier = Modifier.testTag(TestTag.Onboarding.DataProtectionScreen),
                    title = stringResource(R.string.onb_data_consent),
                    onBack = { navController.popBackStack() },
                    url = URI_DATA_TERMS
                )
            }
        }
    }
}

@Suppress("LongMethod")
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun OnboardingScreenWithScaffold(
    navController: NavController,
    secureMethod: OnboardingSecureAppMethod,
    onSecureMethodChange: (OnboardingSecureAppMethod) -> Unit,
    allowTracking: Boolean,
    onAllowTracking: (Boolean) -> Unit,
    onSaveNewUser: (
        allowTracking: Boolean,
        defaultProfileName: String,
        secureAppMethod: OnboardingSecureAppMethod
    ) -> Unit
) {
    val context = LocalContext.current

    val defaultProfileName = stringResource(R.string.onboarding_default_profile_name)

    Box {
        var page by rememberSaveable { mutableStateOf(OnboardingPages.Welcome) }

        LaunchedEffect(secureMethod) {
            if (secureMethod is OnboardingSecureAppMethod.DeviceSecurity && page == OnboardingPages.SecureApp) {
                page = OnboardingPages.Analytics
            }
        }

        BackHandler(enabled = page.index > 1) {
            page = OnboardingPages.pageOf(page.index - 1)
        }

        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            targetState = page,
            transitionSpec = {
                when {
                    initialState == OnboardingPages.Welcome &&
                        targetState == OnboardingPages.pageOf(1) -> {
                        fadeIn(tween(durationMillis = 770)) with fadeOut(tween(durationMillis = 770))
                    }

                    initialState.index > targetState.index -> {
                        slideIntoContainer(AnimatedContentScope.SlideDirection.Right) with
                            slideOutOfContainer(AnimatedContentScope.SlideDirection.Right)
                    }

                    else -> {
                        slideIntoContainer(AnimatedContentScope.SlideDirection.Left) with
                            slideOutOfContainer(AnimatedContentScope.SlideDirection.Left)
                    }
                }
            }
        ) {
            when (it) {
                OnboardingPages.Welcome -> {
                    OnboardingWelcome(
                        onNextPage = {
                            page = OnboardingPages.DataProtection
                        }
                    )
                }

                OnboardingPages.DataProtection -> {
                    OnboardingPageTerms(
                        navController = navController,
                        onNextPage = {
                            page = OnboardingPages.SecureApp
                        }
                    )
                }

                OnboardingPages.SecureApp -> {
                    OnboardingSecureApp(
                        secureMethod = secureMethod,
                        onSecureMethodChange = onSecureMethodChange,
                        onOpenBiometricScreen = {
                            navController.navigate(OnboardingNavigationScreens.Biometry.path())
                        },
                        onNextPage = {
                            page = OnboardingPages.Analytics
                        }
                    )
                }

                OnboardingPages.Analytics -> {
                    val disAllowToast = stringResource(R.string.settings_tracking_disallow_info)
                    OnboardingPageAnalytics(
                        allowAnalytics = allowTracking,
                        onAllowAnalytics = {
                            if (!it) {
                                onAllowTracking(false)
                                createToastShort(context, disAllowToast)
                            } else {
                                navController.navigate(OnboardingNavigationScreens.Analytics.path())
                            }
                        },
                        onNextPage = {
                            onSaveNewUser(allowTracking, defaultProfileName, secureMethod)
                        }
                    )
                }
            }
        }

        if (BuildKonfig.INTERNAL) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .systemBarsPadding()
                    .padding(PaddingDefaults.Medium)
            ) {
                OutlinedDebugButton(
                    "SKIP",
                    onClick = {
                        onSaveNewUser(false, defaultProfileName, OnboardingSecureAppMethod.Password("a", "a", 9))
                    }
                )
            }
        }
    }
}

@Composable
private fun OnboardingWelcome(
    onNextPage: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(timeMillis = 1770)
        onNextPage()
    }

    Column(
        modifier = Modifier
            .testTag(TestTag.Onboarding.WelcomeScreen)
            .padding(horizontal = PaddingDefaults.Medium)
            .systemBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .padding(
                    top = PaddingDefaults.Medium
                )
                .align(Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painterResource(R.drawable.ic_onboarding_logo_flag),
                null,
                modifier = Modifier.padding(end = 10.dp)
            )
            Icon(
                painterResource(R.drawable.ic_onboarding_logo_gematik),
                null,
                tint = AppTheme.colors.primary900
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .semantics(mergeDescendants = true) {}
        ) {
            Image(
                painterResource(R.drawable.erp_logo),
                null,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = PaddingDefaults.Large)
            )
            Text(
                text = stringResource(R.string.app_name),
                style = AppTheme.typography.h4,
                fontWeight = FontWeight.W700,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(
                        top = PaddingDefaults.Medium,
                        bottom = PaddingDefaults.Small
                    )
            )
            Text(
                text = stringResource(R.string.on_boarding_page_1_header),
                style = AppTheme.typography.subtitle1l,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(
                        bottom = PaddingDefaults.XXLarge
                    )
            )
        }

        @Suppress("MagicNumber")
        Image(
            painterResource(R.drawable.onboarding_boygrannygranpa),
            null,
            alignment = Alignment.BottomStart,
            modifier = Modifier.fillMaxSize().offset(x = (-60).dp)
        )
    }
}

@Composable
private fun OnboardingPageAnalytics(
    allowAnalytics: Boolean,
    onAllowAnalytics: (Boolean) -> Unit,
    onNextPage: () -> Unit
) {
    OnboardingScaffold(
        state = rememberLazyListState(),
        bottomBar = {
            OnboardingBottomBar(
                info = stringResource(R.string.onboarding_analytics_bottom_you_can_change),
                buttonText = stringResource(R.string.onboarding_bottom_button_next),
                buttonEnabled = true,
                onButtonClick = onNextPage
            )
        },
        modifier = Modifier
            .visualTestTag(TestTag.Onboarding.AnalyticsScreen)
            .fillMaxSize()
    ) {
        item {
            SpacerXXLarge()
            Text(
                text = stringResource(R.string.onb_page_5_header),
                style = AppTheme.typography.h4,
                fontWeight = FontWeight.W700,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .padding(
                        top = PaddingDefaults.XXLarge,
                        bottom = PaddingDefaults.Large
                    )
            )
            SpacerXXLarge()
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)) {
                Text(
                    text = stringResource(R.string.onboarding_analytics_we_want),
                    style = AppTheme.typography.subtitle1
                )
                AnalyticsInfo(
                    icon = Icons.Rounded.Star,
                    text = stringResource(R.string.onboarding_analytics_ww_usability)
                )
                AnalyticsInfo(
                    icon = Icons.Rounded.FlashOn,
                    text = stringResource(R.string.onboarding_analytics_ww_errors)
                )
                AnalyticsInfo(
                    icon = Icons.Rounded.PersonPin,
                    text = stringResource(R.string.onboarding_analytics_ww_anon)
                )
            }
            SpacerXXLarge()
        }
        item {
            AnalyticsToggle(allowAnalytics, onAllowAnalytics)
            SpacerMedium()
        }
    }
}

@Composable
private fun OnboardingPageTerms(
    navController: NavController,
    onNextPage: () -> Unit
) {
    var accepted by rememberSaveable { mutableStateOf(false) }

    OnboardingScaffold(
        state = rememberLazyListState(),
        bottomBar = {
            OnboardingBottomBar(
                modifier = Modifier.fillMaxWidth(),
                info = null,
                buttonText = stringResource(R.string.onboarding_bottom_button_accept),
                buttonEnabled = accepted,
                onButtonClick = onNextPage
            )
        },
        modifier = Modifier
            .visualTestTag(TestTag.Onboarding.DataTermsScreen)
            .fillMaxSize()
    ) {
        item {
            SpacerXXLarge()
            Image(
                painter = painterResource(R.drawable.paragraph),
                contentDescription = null,
                alignment = Alignment.CenterStart,
                modifier = Modifier.fillMaxWidth()
            )
            SpacerXXLarge()
        }
        item {
            Text(
                text = stringResource(R.string.onb_page_4_header),
                style = AppTheme.typography.h4,
                fontWeight = FontWeight.W700,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(bottom = PaddingDefaults.Medium, top = PaddingDefaults.XXLarge)
            )
            SpacerMedium()
        }
        item {
            SecondaryButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    navController.navigate(OnboardingNavigationScreens.DataProtection.path())
                }
            ) {
                Text(stringResource(R.string.onboarding_data_button))
            }
            SpacerMedium()
        }
        item {
            SecondaryButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    navController.navigate(OnboardingNavigationScreens.TermsOfUse.path())
                }
            ) {
                Text(stringResource(R.string.onboarding_terms_button))
            }
            SpacerXXLarge()
        }
        item {
            DataTermsToggle(
                accepted = accepted,
                onCheckedChange = {
                    accepted = it
                }
            )
            SpacerMedium()
        }
    }
}

@Composable
private fun AnalyticsInfo(icon: ImageVector, text: String) {
    Row(Modifier.fillMaxWidth()) {
        Icon(icon, null, tint = AppTheme.colors.primary600)
        SpacerMedium()
        Text(
            text = text,
            style = AppTheme.typography.body1
        )
    }
}

@Composable
private fun AnalyticsToggle(
    analyticsAllowed: Boolean,
    onCheckedChange: (Boolean) -> Unit
) =
    LargeToggle(
        text = stringResource(R.string.on_boarding_page_5_label),
        checked = analyticsAllowed,
        onCheckedChange = onCheckedChange
    )

@Composable
private fun DataTermsToggle(
    accepted: Boolean,
    onCheckedChange: (Boolean) -> Unit
) =
    LargeToggle(
        text = stringResource(R.string.onboarding_data_terms_info),
        checked = accepted,
        onCheckedChange = onCheckedChange
    )

@Composable
private fun LargeToggle(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(PaddingDefaults.Medium))
            .background(AppTheme.colors.neutral100, shape = RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                enabled = true,
                role = Role.Switch,
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            )
            .padding(PaddingDefaults.Medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
    ) {
        Switch(
            checked = checked,
            onCheckedChange = null
        )
        SpacerSmall()
        Text(
            text = text,
            style = AppTheme.typography.subtitle2,
            modifier = Modifier.weight(1f)
        )
    }
}
