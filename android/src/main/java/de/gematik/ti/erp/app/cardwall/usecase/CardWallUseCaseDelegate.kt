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

package de.gematik.ti.erp.app.cardwall.usecase

import de.gematik.ti.erp.app.cardwall.ui.model.CardWallData
import de.gematik.ti.erp.app.demo.usecase.DemoUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class CardWallUseCaseDelegate @Inject constructor(
    private val demoDelegate: CardWallUseCaseDemo,
    private val productionDelegate: CardWallUseCaseProduction,
    private val demoUseCase: DemoUseCase
) : CardWallUseCase {
    private val delegate: CardWallUseCase
        get() = if (demoUseCase.isDemoModeActive) demoDelegate else productionDelegate

    override var cardWallIntroIsAccepted: Boolean by delegate::cardWallIntroIsAccepted
    override var deviceHasNFCAndAndroidMOrHigher: Boolean by delegate::deviceHasNFCAndAndroidMOrHigher
    override val deviceHasNFCEnabled: Boolean by delegate::deviceHasNFCEnabled

    override suspend fun cardAccessNumberWasSaved(): Flow<Boolean> =
        delegate.cardAccessNumberWasSaved()

    override suspend fun getAuthenticationMethod(profileName: String): CardWallData.AuthenticationMethod =
        delegate.getAuthenticationMethod(profileName)

    override suspend fun setCardAccessNumber(can: String?) {
        delegate.setCardAccessNumber(can)
    }

    override fun cardAccessNumber(): Flow<String?> =
        delegate.cardAccessNumber()
}
