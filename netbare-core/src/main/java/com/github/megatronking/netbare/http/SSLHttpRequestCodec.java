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

import com.github.megatronking.netbare.NetBareLog;
import com.github.megatronking.netbare.ssl.SSLEngineFactory;
import com.github.megatronking.netbare.ssl.SSLRequestCodec;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import javax.net.ssl.SSLEngine;

/**
 * Http request ssl codec enables Application-Layer Protocol Negotiation(ALPN).
 *
 * See http://tools.ietf.org/html/draft-agl-tls-nextprotoneg-04#page-4
 *
 * @author Megatron King
 * @since 2019/1/3 23:01
 */
/* package */ class SSLHttpRequestCodec extends SSLRequestCodec {

    private HttpProtocol mSelectedAlpnProtocol;
    private boolean mAlpnEnabled;
    private boolean mSelectedAlpnResolved;

    /* package */ SSLHttpRequestCodec(SSLEngineFactory factory) {
        super(factory);
    }

    @Override
    protected SSLEngine createEngine(SSLEngineFactory factory) {
        return enableALPN(super.createEngine(factory));
    }

    public void setSelectedAlpnProtocol(HttpProtocol protocol) {
        mSelectedAlpnProtocol = protocol;
    }

    public void setSelectedAlpnResolved() {
        mSelectedAlpnResolved = true;
    }

    public boolean selectedAlpnResolved() {
        return mSelectedAlpnResolved;
    }

    private SSLEngine enableALPN(SSLEngine sslEngine) {
        if (sslEngine == null) {
            return null;
        }
        if (mAlpnEnabled) {
            return sslEngine;
        }
        mAlpnEnabled = true;
        // Nothing to enable.
        if (mSelectedAlpnProtocol == null) {
            return sslEngine;
        }
        try {
            String sslEngineName = sslEngine.getClass().getSimpleName();
            if (sslEngineName.equals("Java8EngineWrapper")) {
                enableJava8EngineWrapperAlpn(sslEngine);
            } else if (sslEngineName.equals("ConscryptEngine")) {
                enableConscryptEngineAlpn(sslEngine);
            } else {
                enableOpenSSLEngineImplAlpn(sslEngine);
            }
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException |
                InvocationTargetException e) {
            NetBareLog.wtf(e);
        }
        return sslEngine;
    }

    private void enableJava8EngineWrapperAlpn(SSLEngine sslEngine) throws NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException {
        Method setApplicationProtocolsMethod = sslEngine.getClass().getDeclaredMethod(
                "setApplicationProtocols", String[].class);
        setApplicationProtocolsMethod.setAccessible(true);
        String[] protocols = {mSelectedAlpnProtocol.toString().toLowerCase()};
        setApplicationProtocolsMethod.invoke(sslEngine, new Object[]{protocols});
    }

    private void enableConscryptEngineAlpn(SSLEngine sslEngine) throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        Method setAlpnProtocolsMethod = sslEngine.getClass().getDeclaredMethod(
                "setAlpnProtocols", String[].class);
        setAlpnProtocolsMethod.setAccessible(true);
        String[] protocols = {mSelectedAlpnProtocol.toString().toLowerCase()};
        setAlpnProtocolsMethod.invoke(sslEngine, new Object[]{protocols});
    }

    private void enableOpenSSLEngineImplAlpn(SSLEngine sslEngine) throws NoSuchFieldException,
            IllegalAccessException {
        Field sslParametersField = sslEngine.getClass().getDeclaredField("sslParameters");
        sslParametersField.setAccessible(true);
        Object sslParameters = sslParametersField.get(sslEngine);
        if (sslParameters != null) {
            Field alpnProtocolsField = sslParameters.getClass().getDeclaredField("alpnProtocols");
            alpnProtocolsField.setAccessible(true);
            alpnProtocolsField.set(sslParameters, concatLengthPrefixed());
        }
    }


    private byte[] concatLengthPrefixed() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String protocolStr = mSelectedAlpnProtocol.toString().toLowerCase();
        os.write(protocolStr.length());
        os.write(protocolStr.getBytes(Charset.forName("UTF-8")), 0, protocolStr.length());
        return os.toByteArray();
    }


}
