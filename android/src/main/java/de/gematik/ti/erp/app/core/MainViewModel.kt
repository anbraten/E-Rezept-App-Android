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

package de.gematik.ti.erp.app.core

import android.net.Uri
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.gematik.ti.erp.app.attestation.usecase.SafetynetUseCase
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.settings.usecase.SettingsUseCase
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDate

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsUseCase: SettingsUseCase,
    safetynetUseCase: SafetynetUseCase,
    private val profilesUseCase: ProfilesUseCase,
) : BaseViewModel() {
    var externalAuthorizationUri: Uri? = null
    val zoomEnabled by settingsUseCase::zoomEnabled
    val authenticationMethod by settingsUseCase::authenticationMethod
    var isNewUser by settingsUseCase::isNewUser

    private var insecureDevicePromptShown = false
    val showInsecureDevicePrompt = settingsUseCase
        .showInsecureDevicePrompt
        .map {
            if (isNewUser) {
                false
            } else if (!insecureDevicePromptShown) {
                insecureDevicePromptShown = true
                it
            } else {
                false
            }
        }

    var showDataTermsUpdate = settingsUseCase.showDataTermsUpdate

    private var safetynetPromptShown = false
    val showSafetynetPrompt =
        safetynetUseCase.runSafetynetAttestation()
            .map {
                if (!it && !safetynetPromptShown) {
                    safetynetPromptShown = true
                    false
                } else {
                    true
                }
            }

    val showProfileSetupPrompt =
        profilesUseCase.isProfileSetupCompleted()
            .map { ! it }

    fun onAcceptInsecureDevice() {
        viewModelScope.launch {
            settingsUseCase.acceptInsecureDevice()
        }
    }

    fun overwriteDefaultProfile(profileName: String) {
        viewModelScope.launch {
            profilesUseCase.overwriteDefaultProfileName(profileName)
        }
    }

    fun acceptUpdatedDataTerms(date: LocalDate) {
        viewModelScope.launch {
            settingsUseCase.updatedDataTermsAccepted(date)
        }
    }

    fun dataProtectionVersionAccepted() =
        settingsUseCase.dataProtectionVersionAccepted()
}
