/*  NetBare - An android network capture and injection library.
 *  Copyright (C) 2018-2019 Megatron King
 *  Copyright (C) 2018-2019 GuoShi
 *
 *  NetBare is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU General Public License as published by the Free Software Found-
 *  ation, either version 3 of the License, or (at your option) any later version.
 *
 *  NetBare is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 *  PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with NetBare.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.megatronking.netbare.ssl;

import androidx.annotation.NonNull;

import com.github.megatronking.netbare.NetBareUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.bouncycastle.operator.OperatorCreationException;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * A factory produces client and server MITM {@link SSLEngine}.
 *
 * @author Megatron King
 * @since 2018-11-10 23:43
 */
public final class SSLEngineFactory {

    private static final int ALIVE_MINUTES = 10;
    private static final int CONCURRENCY_LEVEL = 16;

    /**
     * Enforce TLS 1.2 if available, since it's not default up to Java 8.
     * <p>
     * Java 7 disables TLS 1.1 and 1.2 for clients. From <a href=
     * "http://docs.oracle.com/javase/7/docs/technotes/guides/security/SunProviders.html"
     * >Java Cryptography Architecture Oracle Providers Documentation:</a>
     * Although SunJSSE in the Java SE 7 release supports TLS 1.1 and TLS 1.2,
     * neither version is enabled by default for client connections. Some
     * servers do not implement forward compatibility correctly and refuse to
     * talk to TLS 1.1 or TLS 1.2 clients. For interoperability, SunJSSE does
     * not enable TLS 1.1 or TLS 1.2 by default for client connections.
     */
    private static final String SSL_CONTEXT_PROTOCOL = "TLSv1.2";

    /**
     * {@link SSLContext}: Every implementation of the Java platform is required
     * to support the following standard SSLContext protocol: TLSv1
     */
    private static final String SSL_CONTEXT_FALLBACK_PROTOCOL = "TLSv1";

    private final JKS mJKS;
    private final Cache<String, SSLContext> mServerSSLContexts;
    private final CertificateGenerator mGenerator;

    private Certificate mCaCert;
    private PrivateKey mCaPrivKey;

    /**
     * Constructs the factory with a self-signed certificate.
     *
     * @param jks Java keystore of the self-signed certificate.
     * @throws GeneralSecurityException If a generic security exception has occurred.
     * @throws IOException If an I/O error has occurred.
     */
    public SSLEngineFactory(@NonNull JKS jks) throws GeneralSecurityException, IOException {
        this.mJKS = jks;
        this.mServerSSLContexts = CacheBuilder.newBuilder()
                .expireAfterAccess(ALIVE_MINUTES, TimeUnit.MINUTES)
                .concurrencyLevel(CONCURRENCY_LEVEL)
                .build();
        this.mGenerator = new CertificateGenerator();
        initializeSSLContext();
    }

    /**
     * Create a MITM server {@link SSLEngine} with the remote server host.
     *
     * @param host The remote server host.
     * @return A server {@link SSLEngine} instance.
     * @throws ExecutionException If an execution error has occurred.
     */
    public SSLEngine createServerEngine(@NonNull final String host) throws ExecutionException {
        SSLContext ctx = mServerSSLContexts.get(host, new Callable<SSLContext>() {
            @Override
            public SSLContext call() throws GeneralSecurityException, IOException,
                    OperatorCreationException {
                return createServerContext(host);
            }
        });
        return ctx.createSSLEngine();
    }

    /**
     * Create a client {@link SSLEngine} with the remote server IP and port.
     *
     * @param ip Remote server IP.
     * @param port Remote server port.
     * @return A client {@link SSLEngine} instance.
     * @throws NoSuchAlgorithmException if no Provider supports a SSLContextSpi implementation for
     * the specified protocol.
     * @throws KeyStoreException If creates KeyManager fail.
     * @throws KeyManagementException If creates SSLContext fail.
     */
    public SSLEngine createClientEngine(@NonNull final String ip, int port)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        SSLContext sslContext = createSSLContext();
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new KeyManagementException("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers));
        }
        sslContext.init(null, new TrustManager[] { trustManagers[0] }, null);
        SSLEngine engine = sslContext.createSSLEngine(ip, port);
        List<String> ciphers = new LinkedList<>();
        for (String each : engine.getEnabledCipherSuites()) {
            if (!each.equals("TLS_DHE_RSA_WITH_AES_128_CBC_SHA") &&
                    !each.equals("TLS_DHE_RSA_WITH_AES_256_CBC_SHA")) {
                ciphers.add(each);
            }
        }
        engine.setEnabledCipherSuites(ciphers.toArray(new String[ciphers.size()]));
        engine.setUseClientMode(true);
        engine.setNeedClientAuth(false);
        return engine;
    }

    private void initializeSSLContext() throws GeneralSecurityException, IOException {
        KeyStore ks = loadKeyStore();
        mCaCert = ks.getCertificate(mJKS.alias());
        mCaPrivKey = (PrivateKey) ks.getKey(mJKS.alias(), mJKS.password());
    }

    private KeyStore loadKeyStore() throws GeneralSecurityException, IOException {
        KeyStore ks = KeyStore.getInstance(mGenerator.keyStoreType());
        FileInputStream is = null;
        try {
            is = new FileInputStream(mJKS.aliasFile(JKS.KEY_STORE_FILE_EXTENSION));
            ks.load(is, mJKS.password());
        } finally {
            NetBareUtils.closeQuietly(is);
        }
        return ks;
    }

    private SSLContext createServerContext(String host) throws GeneralSecurityException,
            IOException, OperatorCreationException {
        KeyStore ks = mGenerator.generateServer(host, mJKS, mCaCert, mCaPrivKey);
        KeyManager[] keyManagers = getKeyManagers(ks);
        return createServerContext(keyManagers);
    }

    private SSLContext createServerContext(KeyManager[] keyManagers) throws NoSuchAlgorithmException,
            KeyManagementException {
        SSLContext result = createSSLContext();
        SecureRandom random = new SecureRandom();
        random.setSeed(System.currentTimeMillis() + 1);
        result.init(keyManagers, null, random);
        return result;
    }

    private SSLContext createSSLContext() throws NoSuchAlgorithmException {
        try {
            return SSLContext.getInstance(SSL_CONTEXT_PROTOCOL);
        } catch (NoSuchAlgorithmException e) {
            return SSLContext.getInstance(SSL_CONTEXT_FALLBACK_PROTOCOL);
        }
    }

    private KeyManager[] getKeyManagers(KeyStore keyStore) throws NoSuchAlgorithmException,
            UnrecoverableKeyException, KeyStoreException {
        String keyManAlg = KeyManagerFactory.getDefaultAlgorithm();
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(keyManAlg);
        kmf.init(keyStore, mJKS.password());
        return kmf.getKeyManagers();
    }

}
