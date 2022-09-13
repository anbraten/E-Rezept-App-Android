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

package de.gematik.ti.erp.app.fhir.model

import de.gematik.ti.erp.app.fhir.parser.asInstant
import de.gematik.ti.erp.app.fhir.parser.contained
import de.gematik.ti.erp.app.fhir.parser.containedArrayOrNull
import de.gematik.ti.erp.app.fhir.parser.containedString
import de.gematik.ti.erp.app.fhir.parser.filterWith
import de.gematik.ti.erp.app.fhir.parser.findAll
import de.gematik.ti.erp.app.fhir.parser.profileValue
import de.gematik.ti.erp.app.fhir.parser.stringValue
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant

fun extractAuditEvents(
    bundle: JsonElement,
    save: (id: String, taskId: String?, description: String, timestamp: Instant) -> Unit
): Int {
    val bundleTotal = bundle.containedArrayOrNull("entry")?.size ?: 0
    val resources = bundle
        .findAll(listOf("entry", "resource"))
        .filterWith(
            "meta.profile",
            profileValue("https://gematik.de/fhir/StructureDefinition/ErxAuditEvent", "1.1.1")
        )

    resources.forEach { resource ->
        val id = resource.containedString("id")
        val text = resource.contained("text").containedString("div")
        val taskId = resource
            .findAll(listOf("entity", "what", "identifier"))
            .filterWith("system", stringValue("https://gematik.de/fhir/NamingSystem/PrescriptionID"))
            .firstOrNull()
            ?.containedString("value")

        val timestamp = requireNotNull(resource.contained("recorded").jsonPrimitive.asInstant()) {
            "Audit event field `recorded` missing"
        }

        val description = text.removeSurrounding("<div xmlns=\"http://www.w3.org/1999/xhtml\">", "</div>")

        save(id, taskId, description, timestamp)
    }

    return bundleTotal
}
