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

package de.gematik.ti.erp.app.prescription.model

import de.gematik.ti.erp.app.db.entities.v1.task.CommunicationProfileV1
import java.time.Duration
import java.time.Instant

val CommunicationWaitStateDelta: Duration = Duration.ofMinutes(10)

// gemSpec_FD_eRp: A_21267 Prozessparameter - Berechtigungen für Nutzer
const val DIRECT_ASSIGNMENT_INDICATOR = "169" // direct assignment taskID starts with 169

object SyncedTaskData {
    enum class TaskStatus {
        Ready, InProgress, Completed, Other, Draft, Requested, Received, Accepted, Rejected, Canceled, OnHold, Failed;
    }

    enum class CommunicationProfile {
        ErxCommunicationDispReq, ErxCommunicationReply;

        fun toEntityValue() = when (this) {
            CommunicationProfile.ErxCommunicationDispReq ->
                CommunicationProfileV1.ErxCommunicationDispReq

            CommunicationProfile.ErxCommunicationReply ->
                CommunicationProfileV1.ErxCommunicationReply
        }.name
    }

    data class SyncedTask(
        val profileId: String,
        val taskId: String,
        val accessCode: String?,
        val lastModified: Instant,
        val organization: Organization,
        val practitioner: Practitioner,
        val patient: Patient,
        val insuranceInformation: InsuranceInformation,
        val expiresOn: Instant?,
        val acceptUntil: Instant?,
        val authoredOn: Instant,
        val status: TaskStatus,
        val medicationRequest: MedicationRequest,
        val medicationDispenses: List<MedicationDispense> = emptyList(),
        val communications: List<Communication> = emptyList()
    ) {
        sealed interface TaskState

        data class Ready(val expiresOn: Instant, val acceptUntil: Instant) : TaskState
        data class LaterRedeemable(val redeemableOn: Instant) : TaskState

        data class Pending(val sentOn: Instant, val toTelematikId: String) : TaskState
        data class InProgress(val lastModified: Instant) : TaskState
        data class Expired(val expiredOn: Instant) : TaskState

        data class Other(val state: TaskStatus, val lastModified: Instant) : TaskState

        fun state(now: Instant = Instant.now(), delta: Duration = CommunicationWaitStateDelta): TaskState =
            when {
                medicationRequest.multiplePrescriptionInfo.indicator &&
                    medicationRequest.multiplePrescriptionInfo.start?.let { start ->
                    start > now
                } == true -> {
                    LaterRedeemable(medicationRequest.multiplePrescriptionInfo.start)
                }

                expiresOn != null && expiresOn < now -> Expired(expiresOn)
                status == TaskStatus.Ready &&
                    accessCode != null &&
                    communications.any { it.profile == CommunicationProfile.ErxCommunicationDispReq } &&
                    redeemState(now, delta) == RedeemState.NotRedeemable -> {
                    val comm = this.communications
                        .filter { it.profile == CommunicationProfile.ErxCommunicationDispReq }
                        .maxBy { it.sentOn }

                    Pending(
                        sentOn = comm.sentOn,
                        toTelematikId = comm.recipient
                    )
                }

                status == TaskStatus.Ready -> Ready(
                    expiresOn = requireNotNull(expiresOn),
                    acceptUntil = requireNotNull(acceptUntil)
                )

                status == TaskStatus.InProgress -> InProgress(lastModified = this.lastModified)
                else -> Other(this.status, this.lastModified)
            }

        enum class RedeemState {
            NotRedeemable,
            RedeemableAndValid,
            RedeemableAfterDelta;

            fun isRedeemable() = this != NotRedeemable
        }

        fun redeemedOn() =
            if (status == TaskStatus.Completed) {
                medicationDispenses.firstOrNull()?.whenHandedOver ?: lastModified
            } else {
                null
            }

        /**
         * The list of redeemable prescriptions. Should NOT be used as a filter for the active/archive tab!
         * See [isActive] for a decision it this prescription should be shown in the "Active" or "Archive" tab.
         */
        fun redeemState(now: Instant = Instant.now(), delta: Duration = Duration.ofMinutes(10)): RedeemState {
            val expired = (expiresOn != null && expiresOn <= now)
            val redeemableLater = medicationRequest.multiplePrescriptionInfo.indicator &&
                medicationRequest.multiplePrescriptionInfo.start?.let {
                it > now
            } == true
            val ready = status == TaskStatus.Ready
            val valid = accessCode != null
            val latestDispenseReqCommunication = communications
                .filter { it.profile == CommunicationProfile.ErxCommunicationDispReq }
                .maxOfOrNull { it.sentOn }
            val isDeltaLocked = latestDispenseReqCommunication?.let { (it + delta) > now }

            return when {
                redeemableLater || expired -> RedeemState.NotRedeemable
                ready && valid && latestDispenseReqCommunication == null -> RedeemState.RedeemableAndValid
                ready && valid && isDeltaLocked == false -> RedeemState.RedeemableAfterDelta
                ready && valid && isDeltaLocked == true -> RedeemState.NotRedeemable
                else -> RedeemState.NotRedeemable
            }
        }

        fun isActive(now: Instant = Instant.now()): Boolean {
            val notExpired = (expiresOn != null && now <= expiresOn) || expiresOn == null
            val allowedStatus = status == TaskStatus.Ready || status == TaskStatus.InProgress
            return notExpired && allowedStatus
        }

        fun isDirectAssignment() =
            taskId.startsWith(DIRECT_ASSIGNMENT_INDICATOR)

        fun isDeletable() =
            when {
                isDirectAssignment() -> status == TaskStatus.Completed
                else -> true
            }

        fun medicationRequestMedicationName() =
            when (medicationRequest.medication) {
                is MedicationPZN -> medicationRequest.medication.text
                is MedicationCompounding -> medicationRequest.medication.form
                is MedicationIngredient -> medicationRequest.medication.ingredients[0].text
                is MedicationFreeText -> medicationRequest.medication.text

                else -> ""
            }

        fun medicationName() = medicationRequestMedicationName()
        fun organizationName() = organization.name ?: practitioner.name
    }

    data class Address(
        val line1: String,
        val line2: String,
        val postalCodeAndCity: String
    ) {
        fun joinToString(): String =
            listOf(
                this.line1,
                this.line2,
                this.postalCodeAndCity
            ).filter {
                it.isNotEmpty()
            }.joinToString(", ")
    }

    data class Organization(
        val name: String? = null,
        val address: Address? = null,
        val uniqueIdentifier: String? = null,
        val phone: String? = null,
        val mail: String? = null
    )

    data class Practitioner(
        val name: String?,
        val qualification: String?,
        val practitionerIdentifier: String?
    )

    data class Patient(
        val name: String?,
        val address: Address?,
        val birthdate: Instant?,
        val insuranceIdentifier: String?
    )

    data class InsuranceInformation(
        val name: String? = null,
        val status: String? = null
    )

    enum class AdditionalFee(val value: String?) {
        None(null),
        NotExempt("0"),
        Exempt("1"),
        ArtificialFertilization("2");

        companion object {
            fun valueOf(v: String?) =
                values().find {
                    it.value == v
                } ?: None
        }
    }

    data class MedicationRequest(
        val medication: Medication? = null,
        val dateOfAccident: Instant? = null,
        val location: String? = null,
        val emergencyFee: Boolean? = null,
        val substitutionAllowed: Boolean,
        val dosageInstruction: String? = null,
        val multiplePrescriptionInfo: MultiplePrescriptionInfo,
        val note: String?,
        val bvg: Boolean? = null,
        val additionalFee: AdditionalFee = AdditionalFee.valueOf(null)
    )

    data class MultiplePrescriptionInfo(
        val indicator: Boolean = false,
        val numbering: Ratio? = null,
        val start: Instant? = null
    )

    data class MedicationDispense(
        val dispenseId: String?,
        val patientIdentifier: String,
        val medication: Medication?,
        val wasSubstituted: Boolean,
        val dosageInstruction: String?,
        val performer: String,
        val whenHandedOver: Instant
    )

    enum class MedicationCategory {
        ARZNEI_UND_VERBAND_MITTEL,
        BTM,
        AMVV;
    }

    data class Quantity(
        val value: String,
        val unit: String
    )

    data class Ratio(
        val numerator: Quantity?,
        val denominator: Quantity?
    )

    data class Ingredient(
        var text: String,
        var form: String?,
        var amount: String?,
        var strength: Ratio?
    )

    sealed interface Medication {
        val category: MedicationCategory
        val vaccine: Boolean
        val text: String
        val form: String?
        val lotNumber: String?
        val expirationDate: Instant?
    }

    data class MedicationFreeText(
        override val category: MedicationCategory,
        override val vaccine: Boolean,
        override val text: String,
        override val form: String?,
        override val lotNumber: String?,
        override val expirationDate: Instant?
    ) : Medication

    data class MedicationIngredient(
        override val category: MedicationCategory,
        override val vaccine: Boolean,
        override val text: String,
        override val form: String?,
        override val lotNumber: String?,
        override val expirationDate: Instant?,
        val normSizeCode: String?,
        val amount: Ratio?,
        val ingredients: List<Ingredient>

    ) : Medication

    data class MedicationCompounding(
        override val category: MedicationCategory,
        override val vaccine: Boolean,
        override val text: String,
        override val form: String?,
        override val lotNumber: String?,
        override val expirationDate: Instant?,
        val manufacturingInstructions: String?,
        val packaging: String?,
        val amount: Ratio?,
        val ingredients: List<Ingredient>

    ) : Medication

    data class MedicationPZN(
        override val category: MedicationCategory,
        override val vaccine: Boolean,
        override val text: String,
        override val form: String?,
        override val lotNumber: String?,
        override val expirationDate: Instant?,
        val uniqueIdentifier: String,
        val normSizeCode: String?,
        val amount: Ratio?
    ) : Medication

    data class Communication(
        val taskId: String,
        val communicationId: String,
        val orderId: String,
        val profile: CommunicationProfile,
        val sentOn: Instant,
        val sender: String,
        val recipient: String,
        val payload: String?,
        val consumed: Boolean
    )
}
