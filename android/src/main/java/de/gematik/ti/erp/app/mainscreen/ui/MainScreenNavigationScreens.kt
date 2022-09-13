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

import android.os.Parcelable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kotlinx.serialization.Serializable
import de.gematik.ti.erp.app.AppNavTypes
import de.gematik.ti.erp.app.Route
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.parcelize.Parcelize

@Parcelize
@Serializable
data class TaskIds(val ids: List<String>) : Parcelable, List<String> by ids

object MainNavigationScreens {
    object Onboarding : Route("Onboarding")
    object ReturningUserSecureAppOnboarding : Route("ReturningUserSecureAppOnboarding")
    object Biometry : Route("Biometry")
    object Settings : Route("Settings")
    object Camera : Route("Camera")
    object Prescriptions : Route("Prescriptions")
    object PrescriptionDetail :
        Route(
            "PrescriptionDetail",
            navArgument("taskId") { type = NavType.StringType }
        ) {
        fun path(taskId: String) = path("taskId" to taskId)
    }

    object Orders : Route("Orders")

    object Messages : Route(
        "Messages",
        navArgument("orderId") { type = NavType.StringType }
    ) {
        fun path(orderId: String) =
            Messages.path("orderId" to orderId)
    }

    object Pharmacies : Route(
        "Pharmacies",
        navArgument("taskIds") {
            type = AppNavTypes.TaskIdsType
            defaultValue = TaskIds(emptyList())
        }
    ) {
        fun path(taskIds: TaskIds) = path("taskIds" to taskIds)
    }

    object RedeemLocally :
        Route("RedeemLocally", navArgument("taskIds") { type = AppNavTypes.TaskIdsType }) {
        fun path(taskIds: TaskIds) = path("taskIds" to taskIds)
    }

    object CardWall : Route(
        "CardWall",
        navArgument("profileId") { type = NavType.StringType }
    ) {
        fun path(profileId: ProfileIdentifier) = path("profileId" to profileId)
    }

    object InsecureDeviceScreen : Route("InsecureDeviceScreen")
    object DataTermsUpdateScreen : Route("DataTermsUpdateScreen")
    object DataProtection : Route("DataProtection")
    object SafetynetNotOkScreen : Route("SafetynetInfoScreen")
    object EditProfile :
        Route("EditProfile", navArgument("profileId") { type = NavType.StringType }) {
        fun path(profileId: String) = path("profileId" to profileId)
    }
    object Terms : Route("Terms")
    object Imprint : Route("Imprint")
    object OpenSourceLicences : Route("OpenSourceLicences")
    object AdditionalLicences : Route("AdditionalLicences")
    object AllowAnalytics : Route("AcceptAnalytics")
    object Password : Route("Password")
    object Debug : Route("Debug")
    object OrderHealthCard : Route("OrderHealthCard")

    object UnlockEgk : Route("UnlockEgk", navArgument("changeSecret") { type = NavType.BoolType }) {
        fun path(changeSecret: Boolean) = path("changeSecret" to changeSecret)
    }
}

val MainScreenBottomNavigationItems = listOf(
    MainNavigationScreens.Prescriptions,
    MainNavigationScreens.Orders,
    MainNavigationScreens.Pharmacies,
    MainNavigationScreens.Settings
)
