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

import kotlinx.serialization.json.Json
import java.io.File
import java.time.DayOfWeek
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

const val ResourceBasePath = "src/commonTest/resources/"

private val testBundle by lazy { File("$ResourceBasePath/pharmacy_result_bundle.json").readText() }

class PharmacyMapperTest {
    private val openingTimeA = OpeningTime(LocalTime.parse("08:00:00"), LocalTime.parse("12:00:00"))
    private val openingTimeB = OpeningTime(LocalTime.parse("14:00:00"), LocalTime.parse("18:00:00"))
    private val openingTimeC = OpeningTime(LocalTime.parse("08:00:00"), LocalTime.parse("20:00:00"))
    private val expected = Pharmacy(
        name = "Heide-Apotheke",
        address = PharmacyAddress(
            lines = listOf("Langener Landstraße 266"),
            postalCode = "27578",
            city = "Bremerhaven"
        ),
        location = Location(latitude = 8.597412, longitude = 53.590027),
        contacts = PharmacyContacts(
            phone = "0471/87029",
            mail = "info@heide-apotheke-bremerhaven.de",
            url = "http://www.heide-apotheke-bremerhaven.de"
        ),
        provides = listOf(
            LocalPharmacyService(
                name = "Heide-Apotheke",
                openingHours = OpeningHours(
                    openingTime = mapOf(
                        DayOfWeek.MONDAY to listOf(openingTimeA, openingTimeB),
                        DayOfWeek.TUESDAY to listOf(openingTimeA, openingTimeB),
                        DayOfWeek.WEDNESDAY to listOf(openingTimeA, openingTimeB),
                        DayOfWeek.THURSDAY to listOf(openingTimeA, openingTimeB),
                        DayOfWeek.FRIDAY to listOf(openingTimeA, openingTimeB),
                        DayOfWeek.SATURDAY to listOf(openingTimeA)
                    )
                )
            ),
            DeliveryPharmacyService(
                name = "Heide-Apotheke",
                openingHours = OpeningHours(
                    openingTime = mapOf(
                        DayOfWeek.MONDAY to listOf(openingTimeC),
                        DayOfWeek.TUESDAY to listOf(openingTimeC),
                        DayOfWeek.WEDNESDAY to listOf(openingTimeC),
                        DayOfWeek.THURSDAY to listOf(openingTimeC),
                        DayOfWeek.FRIDAY to listOf(openingTimeC)
                    )
                )
            ),
            OnlinePharmacyService(
                name = "Heide-Apotheke"
            ),
            PickUpPharmacyService(
                name = "Heide-Apotheke"
            )
        ),
        telematikId = "3-05.2.1007600000.080",
        ready = true
    )

    @Test
    fun `map pharmacies from JSON bundle`() {
        val pharmacies = extractPharmacyServices(
            Json.parseToJsonElement(testBundle),
            onError = { element, cause ->
                println(element)
                throw cause
            }
        ).pharmacies

        assertEquals(10, pharmacies.size)

        assertEquals(expected, pharmacies[0])
    }
}
