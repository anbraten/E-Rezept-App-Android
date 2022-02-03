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

package de.gematik.ti.erp.app.nfc.model

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.DLApplicationSpecific
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.math.ec.ECCurve
import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

/**
 * Utility class for card functions
 */
object CardUtilities {
    private const val UNCOMPRESSEDPOINTVALUE = 0x04

    /**
     * Decodes an ECPoint from byte array. Prime field p is taken from the passed curve
     * The first byte must contain the value 0x04 (uncompressed point).
     *
     * @param byteArray Byte array of the form {0x04 || x-bytes [] || y byte []}
     * @param curve     The curve on which the point should lie.
     * @return EC point generated from input data
     */
    fun byteArrayToECPoint(byteArray: ByteArray, curve: ECCurve): ECPoint {
        return if (byteArray[0] != UNCOMPRESSEDPOINTVALUE.toByte()) {
            throw IllegalArgumentException("Found no uncompressed point!")
        } else {
            val x = ByteArray((byteArray.size - 1) / 2)
            val y = ByteArray((byteArray.size - 1) / 2)

            System.arraycopy(byteArray, 1, x, 0, (byteArray.size - 1) / 2)
            System.arraycopy(
                byteArray, 1 + (byteArray.size - 1) / 2, y, 0,
                (byteArray.size - 1) / 2
            )
            curve.createPoint(BigInteger(1, x), BigInteger(1, y))
        }
    }

    /**
     * Encodes an ASN1 KeyObject
     */
    fun extractKeyObjectEncoded(asn1Input: ByteArray): ByteArray =
        ASN1InputStream(asn1Input).use { asn1InputStream ->
            val seq = asn1InputStream.readObject() as DLApplicationSpecific
            val seqObj: ASN1Object = seq.getObject()
            seqObj.encoded.copyOfRange(2, seqObj.encoded.size)
        }
}

fun ByteArray.toX509Certificate() =
    CertificateFactory.getInstance("X.509", BouncyCastleProvider()).let {
        it.generateCertificate(this.inputStream()) as X509Certificate
    }
