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
import android.content.Intent;
import android.os.Bundle;
import android.security.KeyChain;
import androidx.annotation.Nullable;

import com.github.megatronking.netbare.NetBareLog;

import java.io.File;
import java.io.IOException;

/**
 * A translucent activity uses to install self-signed certificate.
 *
 * @author Megatron King
 * @since 2018-11-10 21:18
 */
public class CertificateInstallActivity extends Activity {

    private static final int REQUEST_CODE_INSTALL = 1;
    public static final String EXTRA_ALIAS = "jks_alias";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            finish();
            return;
        }
        Intent intent = KeyChain.createInstallIntent();
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_CODE_INSTALL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_INSTALL && resultCode == RESULT_OK) {
            File jsk = new File(getCacheDir(),
                    getIntent().getStringExtra(EXTRA_ALIAS) + JKS.KEY_JKS_FILE_EXTENSION);
            try {
                if(!jsk.exists() && !jsk.createNewFile()) {
                    throw new IOException("Create jks file failed.");
                }
            } catch (IOException e) {
                NetBareLog.wtf(e);
            }
        }
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putAll(getIntent().getExtras());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        getIntent().putExtras(savedInstanceState);
    }

}
