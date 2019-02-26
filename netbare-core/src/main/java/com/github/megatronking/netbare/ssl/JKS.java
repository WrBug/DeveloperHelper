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

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.security.KeyChain;
import androidx.annotation.NonNull;

import com.github.megatronking.netbare.NetBareLog;
import com.github.megatronking.netbare.NetBareUtils;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.security.KeyStore;
import java.security.cert.Certificate;

/**
 * A java keystore to manage root certificate.
 *
 * @author Megatron King
 * @since 2018-11-10 20:06
 */
public class JKS {
    private static final String JSK_ALIAS = "易开发抓包证书";
    private static volatile JKS jks;
    public static final String KEY_STORE_FILE_EXTENSION = ".p12";
    public static final String KEY_PEM_FILE_EXTENSION = ".pem";
    public static final String KEY_JKS_FILE_EXTENSION = ".jks";
    private final File keystoreDir;
    private final String alias;
    private final char[] password;
    private final String commonName;
    private final String organization;
    private final String organizationalUnitName;
    private final String certOrganization;
    private final String certOrganizationalUnitName;

    public JKS(@NonNull Context context, @NonNull String alias, @NonNull char[] password,
               @NonNull String commonName, @NonNull String organization,
               @NonNull String organizationalUnitName, @NonNull String certOrganization,
               @NonNull String certOrganizationalUnitName) {
        this.keystoreDir = context.getCacheDir();
        this.alias = alias;
        this.password = password;
        this.commonName = commonName;
        this.organization = organization;
        this.organizationalUnitName = organizationalUnitName;
        this.certOrganization = certOrganization;
        this.certOrganizationalUnitName = certOrganizationalUnitName;
        createKeystore();
    }

    public static void init(Application application) {
        if (jks == null) {
            synchronized (JKS.class) {
                if (jks == null) {
                    jks = new JKS(application, JSK_ALIAS, JSK_ALIAS.toCharArray(), JSK_ALIAS, JSK_ALIAS, JSK_ALIAS, JSK_ALIAS, JSK_ALIAS);
                }
            }
        }
    }

    public static JKS getJks() {
        return jks;
    }

    public static String getJskAlias() {
        return JSK_ALIAS;
    }

    String alias() {
        return alias;
    }

    char[] password() {
        return password;
    }

    String commonName() {
        return commonName;
    }

    String organization() {
        return organization;
    }

    String organizationalUnitName() {
        return organizationalUnitName;
    }

    String certOrganisation() {
        return certOrganization;
    }

    String certOrganizationalUnitName() {
        return certOrganizationalUnitName;
    }

    public boolean isInstalled() {
        return aliasFile(KEY_STORE_FILE_EXTENSION).exists() &&
                aliasFile(KEY_PEM_FILE_EXTENSION).exists() &&
                aliasFile(KEY_JKS_FILE_EXTENSION).exists();
    }

    public File aliasFile(String fileExtension) {
        return new File(keystoreDir, alias + fileExtension);
    }

    private void createKeystore() {
        if (aliasFile(KEY_STORE_FILE_EXTENSION).exists() &&
                aliasFile(KEY_PEM_FILE_EXTENSION).exists()) {
            return;
        }

        // Generate keystore in the async thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                CertificateGenerator generator = new CertificateGenerator();
                KeyStore keystore;
                OutputStream os = null;
                Writer sw = null;
                JcaPEMWriter pw = null;
                try {
                    keystore = generator.generateRoot(JKS.this);
                    os = new FileOutputStream(aliasFile(KEY_STORE_FILE_EXTENSION));
                    keystore.store(os, password());

                    Certificate cert = keystore.getCertificate(alias());
                    sw = new FileWriter(aliasFile(KEY_PEM_FILE_EXTENSION));
                    pw = new JcaPEMWriter(sw);
                    pw.writeObject(cert);
                    pw.flush();
                    NetBareLog.i("Generate keystore succeed.");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    NetBareUtils.closeQuietly(os);
                    NetBareUtils.closeQuietly(sw);
                    NetBareUtils.closeQuietly(pw);
                }
            }
        }).start();
    }

    /**
     * Whether the certificate with given alias has been installed.
     *
     * @param context Any context.
     * @param alias   Key store alias.
     * @return True if the certificate has been installed.
     */
    public static boolean isInstalled(Context context, String alias) {
        return new File(context.getCacheDir(),
                alias + KEY_JKS_FILE_EXTENSION).exists();
    }

    /**
     * Install the self-signed root certificate.
     *
     * @param context Any context.
     * @param name    Key chain name.
     * @param alias   Key store alias.
     * @throws IOException If an IO error has occurred.
     */
    public static void install(Context context, String name, String alias)
            throws IOException {
        byte[] keychain;
        FileInputStream is = null;
        try {
            is = new FileInputStream(new File(context.getCacheDir(),
                    alias + KEY_PEM_FILE_EXTENSION));
            keychain = new byte[is.available()];
            int len = is.read(keychain);
            if (len != keychain.length) {
                throw new IOException("Install JKS failed, len: " + len);
            }
        } finally {
            NetBareUtils.closeQuietly(is);
        }

        Intent intent = new Intent(context, CertificateInstallActivity.class);
        intent.putExtra(KeyChain.EXTRA_CERTIFICATE, keychain);
        intent.putExtra(KeyChain.EXTRA_NAME, name);
        intent.putExtra(CertificateInstallActivity.EXTRA_ALIAS, alias);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

}
