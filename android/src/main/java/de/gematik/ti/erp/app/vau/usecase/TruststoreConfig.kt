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

package de.gematik.ti.erp.app.vau.usecase

import android.util.Base64
import de.gematik.ti.erp.app.BuildKonfig
import org.bouncycastle.cert.X509CertificateHolder
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TruststoreConfig @Inject constructor() {
    val maxOCSPResponseAge: Duration by lazy {
        Duration.ofHours(BuildKonfig.VAU_OCSP_RESPONSE_MAX_AGE)
    }

    val trustAnchor by lazy {
        X509CertificateHolder(Base64.decode(BuildKonfig.APP_TRUST_ANCHOR_BASE64, Base64.DEFAULT))
    }
}
