/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.crypto.pkix.internal;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Calendar;

import org.bouncycastle.asn1.x509.TBSCertificate;
import org.xwiki.crypto.params.cipher.asymmetric.PublicKeyParameters;
import org.xwiki.crypto.pkix.CertificateGenerator;
import org.xwiki.crypto.pkix.CertifyingSigner;
import org.xwiki.crypto.pkix.params.CertificateParameters;
import org.xwiki.crypto.pkix.params.CertifiedPublicKey;
import org.xwiki.crypto.pkix.params.PrincipalIndentifier;
import org.xwiki.crypto.pkix.params.x509certificate.X509CertificateGenerationParameters;
import org.xwiki.crypto.pkix.params.x509certificate.X509CertificateParameters;
import org.xwiki.crypto.signer.Signer;
import org.xwiki.crypto.signer.SignerFactory;

/**
 * Base class for X.509 certificate generators.
 *
 * @version $Id$
 * @since 5.4
 */
public abstract class AbstractBcX509CertificateGenerator implements CertificateGenerator
{
    private final Signer signer;
    private final int validity;
    private final SignerFactory signerFactory;
    private final SecureRandom random;

    /**
     * Create a initialized certificate generator.
     *
     * @param signer a certifying signer initialized with the certified key pair of the issuer
     *               or a signer initialized with the private key of the subject for creating a self sign certificate.
     * @param parameters the common parameters for all certificate generated by this generator.
     * @param signerFactory the signer factory to be used by the certificate to verify signature.
     * @param random a random source.
     */
    public AbstractBcX509CertificateGenerator(Signer signer, X509CertificateGenerationParameters parameters,
        SignerFactory signerFactory, SecureRandom random)
    {
        this.signer = signer;
        this.validity = parameters.getValidity();
        this.signerFactory = signerFactory;
        this.random = random;
    }

    /**
     * @return a new instance of a TBS certificate builder.
     */
    protected abstract BcX509TBSCertificateBuilder getTBSCertificateBuilder();

    /**
     * Extend TBS certificate depending of certificate version.
     *
     * @param builder the X.509 TBS certificate builder received from #getTBSCertificateBuilder().
     * @param issuer the certified public key of the issuer of the certificate, or null for self signed one.
     * @param subjectName the subject name.
     * @param subject the subject public key.
     * @param parameters the X.509 certificate parameters.
     * @throws IOException on encoding error.
     */
    protected void extendsTBSCertificate(BcX509TBSCertificateBuilder builder, CertifiedPublicKey issuer,
        PrincipalIndentifier subjectName, PublicKeyParameters subject, X509CertificateParameters parameters)
        throws IOException
    {
        // Do nothing by default.
    }

    /**
     * Build the TBS Certificate.
     *
     * @param subjectName the identifier of the public key owner.
     * @param subject the public key to certify.
     * @param parameters the subject parameters for this certificate.
     * @return the TBS certificate.
     * @throws IOException on encoding error.
     */
    public TBSCertificate buildTBSCertificate(PrincipalIndentifier subjectName,
        PublicKeyParameters subject, X509CertificateParameters parameters) throws IOException
    {
        PrincipalIndentifier issuerName;
        CertifiedPublicKey issuer = null;

        if (signer instanceof CertifyingSigner) {
            issuer = ((CertifyingSigner) signer).getCertifier();
            issuerName = issuer.getSubject();
        } else {
            issuerName = subjectName;
        }

        BcX509TBSCertificateBuilder builder = getTBSCertificateBuilder();

        builder.setSerialNumber(new BigInteger(128, random))
               .setIssuer(issuerName);

        addValidityDates(builder);

        extendsTBSCertificate(builder, issuer, subjectName, subject, parameters);

        return builder.setSubject(subjectName)
                      .setSubjectPublicKeyInfo(subject)
                      .setSignature(signer)
                      .build();
    }

    @Override
    public CertifiedPublicKey generate(PrincipalIndentifier subjectName, PublicKeyParameters subject,
        CertificateParameters parameters) throws IOException, GeneralSecurityException
    {
        if (!(parameters instanceof X509CertificateParameters)) {
            throw new IllegalArgumentException("Invalid parameters for X.509 certificate: "
                + parameters.getClass().getName());
        }

        TBSCertificate tbsCert = buildTBSCertificate(subjectName, subject, (X509CertificateParameters) parameters);

        return new BcX509CertifiedPublicKey(BcUtils.getX509CertificateHolder(
            tbsCert,
            BcUtils.updateDEREncodedObject(signer, tbsCert).generate()),
            signerFactory);
    }

    private void addValidityDates(BcX509TBSCertificateBuilder builder)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        builder.setStartDate(cal.getTime());

        cal.add(Calendar.DATE, validity);

        builder.setEndDate(cal.getTime());
    }
}
