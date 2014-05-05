/**
 * This file is part of the hyk-proxy-framework project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: BouncyCastleHelper.java 
 *
 * @author yinqiwen [ 2011-5-8 | 11:27:39AM ]
 *
 */
package org.snova.framework.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.PrincipalUtil;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.MiscPEMGenerator;
import org.bouncycastle.util.io.pem.PemWriter;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snova.framework.config.SnovaConfiguration;

/**
 *
 */
public class SslCertificateHelper
{
	protected static Logger logger = LoggerFactory
	        .getLogger(SslCertificateHelper.class);
	static
	{
		Security.addProvider(new BouncyCastleProvider());
	}

	static PrivateKey caPriKey;
	static X509Certificate caCert;
	static Map<String, KeyStore> kstCache = new WeakHashMap<String, KeyStore>();
	public static final String KS_PASS = "hyk-proxy";
	public static final String CA_ALIAS = "RootCAPriKey";
	public static final String CLIENT_CERT_ALIAS = "FakeCertForClient";
	public static final String CA_FILE = "RootKeyStore.kst";

	public static PrivateKey getFakeRootCAPrivateKey()
	{
		loadFakeRootCA();
		return caPriKey;
	}

	public static X509Certificate getFakeRootCAX509Certificate()
	{
		loadFakeRootCA();
		return caCert;
	}

	private static final byte[] BUFFER = new byte[4096];

	/**
	 * copy input to output stream - available in several StreamUtils or Streams
	 * classes
	 */
	public static void copy(InputStream input, OutputStream output)
	        throws IOException
	{
		int bytesRead;
		while ((bytesRead = input.read(BUFFER)) != -1)
		{
			output.write(BUFFER, 0, bytesRead);
		}
	}

	private static File getFakeSSLCertFile(String host)
	{
		File confhome = new File(SnovaConfiguration.getHome(), "cert");
		if (!confhome.exists())
		{
			confhome.mkdir();
		}
		confhome = new File(confhome, "host");
		if (!confhome.exists())
		{
			confhome.mkdir();
		}
		File file = new File(confhome, host + ".kst");
		return file;
	}

	private static boolean loadFakeRootCA()
	{
		if (null == caPriKey || null == caCert)
		{
			try
			{
				KeyStore ks = KeyStore.getInstance("JKS");
				FileInputStream fis = new FileInputStream(new File(
				        SnovaConfiguration.getHome() + "/cert",
				        "RootKeyStore.kst"));
				ks.load(fis, KS_PASS.toCharArray());
				caCert = (X509Certificate) ks.getCertificate(CA_ALIAS);
				caPriKey = (PrivateKey) ks.getKey(CA_ALIAS,
				        KS_PASS.toCharArray());
				fis.close();

			}
			catch (Exception e)
			{
				logger.error("Failed to load fake CA key store.", e);
				return false;
			}

		}
		return true;
	}

	private static KeyPair createRSAKeyPair() throws NoSuchAlgorithmException,
	        NoSuchProviderException
	{
		KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
		SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
		rnd.setSeed(System.currentTimeMillis());
		g.initialize(2048, rnd);
		KeyPair keypair = g.genKeyPair();
		return keypair;

	}

	public static X509Certificate createCACert(KeyPair keypair)
	        throws InvalidKeyException, IllegalStateException,
	        NoSuchProviderException, NoSuchAlgorithmException,
	        SignatureException, CertificateException
	{
		final Date startDate = Calendar.getInstance().getTime();
		final Date expireDate = new Date(startDate.getTime()
		        + (100L * 365L * 24L * 60L * 60L * 1000L));
		// The Root CA serial number is '1'
		final BigInteger serialNumber = new BigInteger("1");

		final X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
		// using the hash code of the user's name and home path, keeps anonymity
		// but also gives user a chance to distinguish between each other
		final X500Principal x500principal = new X500Principal(
		        "CN = Snova Framework Root Fake CA, "
		                + "L = "
		                + Integer.toHexString(System.getProperty("user.name")
		                        .hashCode())
		                + Integer.toHexString(System.getProperty("user.home")
		                        .hashCode()) + ", "
		                + "O = Snova Root Fake CA, "
		                + "OU = Snova Root Fake CA, " + "C = XX");

		certGen.setSerialNumber(serialNumber);
		certGen.setSubjectDN(x500principal);
		certGen.setIssuerDN(x500principal);
		certGen.setNotBefore(startDate);
		certGen.setNotAfter(expireDate);
		certGen.setPublicKey(keypair.getPublic());
		certGen.setSignatureAlgorithm("SHA1withRSA");

		KeyStore ks = null;
		try
		{
			certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false,
			        new SubjectKeyIdentifierStructure(keypair.getPublic()));
			certGen.addExtension(X509Extensions.BasicConstraints, false,
			        new BasicConstraints(true));
			final X509Certificate cert = certGen.generate(keypair.getPrivate());
			return cert;
		}
		catch (final Exception e)
		{
			throw new IllegalStateException(
			        "Errors during assembling root CA.", e);
		}
	}

	public static X509Certificate createClientCert(String host, PublicKey pubKey)
	        throws Exception
	{
		if (!loadFakeRootCA())
		{
			return null;
		}
		// final KeyPair mykp = this.createKeyPair();
		// final PrivateKey privKey = mykp.getPrivate();
		// final PublicKey pubKey = mykp.getPublic();

		//
		// subjects name table.
		//
		final Hashtable<Object, String> attrs = new Hashtable<Object, String>();
		final Vector<Object> order = new Vector<Object>();

		attrs.put(X509Name.CN, host);
		attrs.put(X509Name.OU, "hyk-proxy Project");
		attrs.put(X509Name.O, "hyk-proxy");
		attrs.put(X509Name.C, "XX");
		attrs.put(X509Name.EmailAddress, "yinqiwen@gmail.com");

		order.addElement(X509Name.CN);
		order.addElement(X509Name.OU);
		order.addElement(X509Name.O);
		order.addElement(X509Name.C);
		order.addElement(X509Name.EmailAddress);

		//
		// create the certificate - version 3
		//
		final X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator();
		v3CertGen.reset();

		v3CertGen
		        .setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
		v3CertGen.setIssuerDN(PrincipalUtil.getSubjectX509Principal(caCert));
		v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60
		        * 60 * 24 * 30));
		v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + 100
		        * (1000L * 60 * 60 * 24 * 30)));
		v3CertGen.setSubjectDN(new X509Principal(order, attrs));
		v3CertGen.setPublicKey(pubKey);
		v3CertGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

		//
		// add the extensions
		//
		v3CertGen.addExtension(X509Extensions.SubjectKeyIdentifier, false,
		        new SubjectKeyIdentifierStructure(pubKey));

		v3CertGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false,
		        new AuthorityKeyIdentifierStructure(caCert.getPublicKey()));

		v3CertGen.addExtension(X509Extensions.BasicConstraints, true,
		        new BasicConstraints(0));

		// X509Certificate cert = v3CertGen.generateX509Certificate(caPrivKey);
		final X509Certificate cert = v3CertGen.generate(caPriKey, "BC");
		cert.checkValidity(new Date());
		cert.verify(caCert.getPublicKey());

		// cert.verify(caCert.getPublicKey());
		// cert.getEncoded();

		return cert;
	}

	public static KeyStore getClientKeyStore(String host) throws Exception
	{
		if (kstCache.containsKey(host))
		{
			return kstCache.get(host);
		}
		KeyStore ks = KeyStore.getInstance("JKS");

		File fakeSslFile = getFakeSSLCertFile(host);
		InputStream is = fakeSslFile.exists() ? new FileInputStream(fakeSslFile)
		        : null;
		//System.out.println("####" + fakeSslFile.getAbsolutePath());
		ks.load(is, null == is ? null : KS_PASS.toCharArray());

		if (null == is)
		{
			final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(Long.toString(System.currentTimeMillis()).getBytes());
			keyGen.initialize(2048, random);
			final KeyPair keypair = keyGen.generateKeyPair();
			// KeyPair pair = createRSAKeyPair();
			X509Certificate cert = createClientCert(host, keypair.getPublic());
			ks.setKeyEntry(CLIENT_CERT_ALIAS, keypair.getPrivate(),
			        KS_PASS.toCharArray(), new Certificate[] { cert, caCert });
			FileOutputStream fos = new FileOutputStream(fakeSslFile);
			ks.store(fos, KS_PASS.toCharArray());
		}
		else
		{
			is.close();
		}
		// ks.store(new FileOutputStream(kst_file), KS_PASS.toCharArray());
		kstCache.put(host, ks);
		return ks;
	}

	public static SSLContext getFakeSSLContext(String host, String port)
	        throws Exception
	{
		long start1 = System.currentTimeMillis();
		KeyStore kst = SslCertificateHelper.getClientKeyStore(host);
		long start = System.currentTimeMillis();
		SSLContext sslContext = SSLContext.getInstance("TLS");
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");

		kmf.init(kst, SslCertificateHelper.KS_PASS.toCharArray());
		sslContext.init(kmf.getKeyManagers(), null, null);
		long end = System.currentTimeMillis();
		if (logger.isDebugEnabled())
		{
			logger.debug("Cost " + (end - start) + "ms to inti ssl context.");
			logger.debug("Cost " + (start - start1)
			        + "ms to retrive key store.");
		}
		// sslparams.s
		// param.setSSLParameters(sslparams);
		return sslContext;
	}

	public static void main(String[] args) throws Exception
	{
		// X509V3CertificateGenerator c = new X509V3CertificateGenerator();
		KeyPair pair = createRSAKeyPair();
		X509Certificate cert = createCACert(pair);
		FileOutputStream fos = new FileOutputStream(CA_FILE);
		// KeyStore.
		// System.out.println("#####" + KeyStore.getDefaultType());
		KeyStore ks = KeyStore.getInstance("bks", "BC");
		ks.load(null, null);
		ks.setKeyEntry(CA_ALIAS, pair.getPrivate(), KS_PASS.toCharArray(),
		        new Certificate[] { cert });
		ks.store(fos, KS_PASS.toCharArray());
		fos.close();
		ks = KeyStore.getInstance("bks", "BC");
		ks.load(new FileInputStream(CA_FILE), KS_PASS.toCharArray());
		Object obj = ks.getCertificate("RootCAPriKey");
		// final Certificate cert =
		// rootca.getCertificate(SslCertificateService.ZAPROXY_JKS_ALIAS);
		final PemWriter pw = new PemWriter(new FileWriter(
		        "Fake-ACRoot-Certificate.cer"));
		pw.writeObject(new MiscPEMGenerator(cert));
		pw.flush();
	}
}
