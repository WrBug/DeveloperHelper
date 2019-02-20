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
package com.github.megatronking.netbare.http;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;

import com.github.megatronking.netbare.NetBareLog;
import com.github.megatronking.netbare.ssl.SSLEngineFactory;
import com.github.megatronking.netbare.ssl.SSLResponseCodec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import javax.net.ssl.SSLEngine;

/**
 * Http SSL codec enables Application-Layer Protocol Negotiation(ALPN).
 *
 * See http://tools.ietf.org/html/draft-agl-tls-nextprotoneg-04#page-4
 *
 * @author Megatron King
 * @since 2019/1/3 23:31
 */
/* package */ class SSLHttpResponseCodec extends SSLResponseCodec {

    private SSLEngine mSSLEngine;

    private boolean mAlpnEnabled;
    private boolean mSelectedAlpnResolved;

    private HttpProtocol[] mClientAlpns;
    private AlpnResolvedCallback mAlpnCallback;

    /* package */ SSLHttpResponseCodec(SSLEngineFactory factory) {
        super(factory);
    }

    @Override
    protected SSLEngine createEngine(SSLEngineFactory factory) {
        if (mSSLEngine == null) {
            mSSLEngine = super.createEngine(factory);
            if (mSSLEngine != null && mClientAlpns != null) {
                enableAlpn();
            }
        }
        return mSSLEngine;
    }

    @Override
    public void decode(ByteBuffer buffer, @NonNull CodecCallback callback) throws IOException {
        super.decode(buffer, callback);
        // ALPN is put in ServerHello, once we receive the remote server packet, the ALPN must be
        // resolved.
        if (!mSelectedAlpnResolved) {
            mAlpnCallback.onResult(getAlpnSelectedProtocol());
        }
        mSelectedAlpnResolved = true;
    }

    public void setSelectedAlpnResolved() {
        mSelectedAlpnResolved = true;
    }

    public void prepareHandshake(HttpProtocol[] protocols, AlpnResolvedCallback callback)
            throws IOException {
        this.mClientAlpns = protocols;
        this.mAlpnCallback = callback;
        super.prepareHandshake();
    }

    private void enableAlpn() {
        try {
            String sslEngineName = mSSLEngine.getClass().getSimpleName();
            if (sslEngineName.equals("Java8EngineWrapper")) {
                enableJava8EngineWrapperAlpn();
            } else if (sslEngineName.equals("ConscryptEngine")) {
                enableConscryptEngineAlpn();
            } else {
                enableOpenSSLEngineImplAlpn();
            }
            mAlpnEnabled = true;
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException |
                InvocationTargetException e) {
            NetBareLog.wtf(e);
        }
    }

    private void enableJava8EngineWrapperAlpn() throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        Method setApplicationProtocolsMethod = mSSLEngine.getClass().getDeclaredMethod(
                "setApplicationProtocols", String[].class);
        setApplicationProtocolsMethod.setAccessible(true);
        String[] protocols = new String[mClientAlpns.length];
        for (int i = 0; i < protocols.length; i++) {
            protocols[i] = mClientAlpns[i].toString().toLowerCase();
        }
        setApplicationProtocolsMethod.invoke(mSSLEngine, new Object[]{protocols});

        Method setUseSessionTicketsMethod = mSSLEngine.getClass().getDeclaredMethod(
                "setUseSessionTickets", boolean.class);
        setUseSessionTicketsMethod.setAccessible(true);
        setUseSessionTicketsMethod.invoke(mSSLEngine, true);
    }

    private void enableConscryptEngineAlpn() throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        Method setAlpnProtocolsMethod = mSSLEngine.getClass().getDeclaredMethod(
                "setAlpnProtocols", String[].class);
        setAlpnProtocolsMethod.setAccessible(true);
        String[] protocols = new String[mClientAlpns.length];
        for (int i = 0; i < protocols.length; i++) {
            protocols[i] = mClientAlpns[i].toString().toLowerCase();
        }
        setAlpnProtocolsMethod.invoke(mSSLEngine, new Object[]{protocols});

        Method setUseSessionTicketsMethod = mSSLEngine.getClass().getDeclaredMethod(
                "setUseSessionTickets", boolean.class);
        setUseSessionTicketsMethod.setAccessible(true);
        setUseSessionTicketsMethod.invoke(mSSLEngine, true);
    }

    private void enableOpenSSLEngineImplAlpn() throws NoSuchFieldException, IllegalAccessException {
        Field sslParametersField = mSSLEngine.getClass().getDeclaredField("sslParameters");
        sslParametersField.setAccessible(true);
        Object sslParameters = sslParametersField.get(mSSLEngine);
        if (sslParameters == null) {
            throw new IllegalAccessException("sslParameters value is null");
        }
        Field useSessionTicketsField = sslParameters.getClass().getDeclaredField("useSessionTickets");
        useSessionTicketsField.setAccessible(true);
        useSessionTicketsField.set(sslParameters, true);
        Field useSniField = sslParameters.getClass().getDeclaredField("useSni");
        useSniField.setAccessible(true);
        useSniField.set(sslParameters, true);
        Field alpnProtocolsField = sslParameters.getClass().getDeclaredField("alpnProtocols");
        alpnProtocolsField.setAccessible(true);
        alpnProtocolsField.set(sslParameters, concatLengthPrefixed(mClientAlpns));
    }

    private byte[] concatLengthPrefixed(HttpProtocol ... protocols) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (HttpProtocol protocol : protocols) {
            String protocolStr = protocol.toString().toLowerCase();
            os.write(protocolStr.length());
            os.write(protocolStr.getBytes(Charset.forName("UTF-8")), 0, protocolStr.length());
        }
        return os.toByteArray();
    }


    @SuppressLint("PrivateApi")
    private String getAlpnSelectedProtocol() {
        if (!mAlpnEnabled) {
            return null;
        }
        String alpnResult = null;
        try {
            String sslEngineName = mSSLEngine.getClass().getSimpleName();
            if (sslEngineName.equals("Java8EngineWrapper")) {
                alpnResult = getJava8EngineWrapperAlpn();
            } else if (sslEngineName.equals("ConscryptEngine")){
                alpnResult = getConscryptEngineAlpn();
            } else {
                alpnResult = getOpenSSLEngineImplAlpn();
            }
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException
                | IllegalAccessException | InvocationTargetException e) {
            NetBareLog.e(e.getMessage());
        }
        return alpnResult;
    }

    private String getJava8EngineWrapperAlpn() throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        Method getApplicationProtocolMethod = mSSLEngine.getClass().getDeclaredMethod(
                "getApplicationProtocol");
        getApplicationProtocolMethod.setAccessible(true);
        return (String) getApplicationProtocolMethod.invoke(mSSLEngine);
    }

    private String getConscryptEngineAlpn() throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        Method getAlpnSelectedProtocolMethod = mSSLEngine.getClass().getDeclaredMethod(
                "getAlpnSelectedProtocol");
        getAlpnSelectedProtocolMethod.setAccessible(true);
        byte[] selectedProtocol = (byte[]) getAlpnSelectedProtocolMethod.invoke(mSSLEngine);
        return selectedProtocol != null ? new String(selectedProtocol, Charset.forName("UTF-8")) : null;
    }

    @SuppressLint("PrivateApi")
    private String getOpenSSLEngineImplAlpn() throws ClassNotFoundException, NoSuchMethodException,
            NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        Class<?> nativeCryptoClass = Class.forName("com.android.org.conscrypt.NativeCrypto");
        Method SSL_get0_alpn_selectedMethod = nativeCryptoClass.getDeclaredMethod(
                "SSL_get0_alpn_selected", long.class);
        SSL_get0_alpn_selectedMethod.setAccessible(true);

        Field sslNativePointerField = mSSLEngine.getClass().getDeclaredField("sslNativePointer");
        sslNativePointerField.setAccessible(true);
        long sslNativePointer = (long) sslNativePointerField.get(mSSLEngine);
        byte[] selectedProtocol = (byte[]) SSL_get0_alpn_selectedMethod.invoke(null, sslNativePointer);
        return selectedProtocol != null ? new String(selectedProtocol, Charset.forName("UTF-8")) : null;
    }

    interface AlpnResolvedCallback {

        void onResult(String selectedAlpnProtocol) throws IOException;

    }

}
