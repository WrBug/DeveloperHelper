/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wrbug.developerhelper.base

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.base.activityresultcallback.ActResultRequest
import com.wrbug.developerhelper.base.activityresultcallback.ActivityResultCallback

/**
 * Various extension functions for AppCompatActivity.
 */


fun AppCompatActivity.setupActionBar(@IdRes toolbarId: Int, action: ActionBar.() -> Unit = {}) {
    setupActionBar(findViewById(toolbarId), action)
}

fun AppCompatActivity.setupActionBar(toolbar: Toolbar, action: ActionBar.() -> Unit = {}) {
    setSupportActionBar(toolbar)
    supportActionBar?.run {
        setDisplayHomeAsUpEnabled(false)
        action()
    }
}

/**
 * Runs a FragmentTransaction, then calls commit().
 */
private inline fun FragmentManager.transact(action: FragmentTransaction.() -> Unit) {
    beginTransaction().apply {
        action()
    }.commit()
}

fun AppCompatActivity.startActivityForResult(intent: Intent, callback: ActivityResultCallback) {
    ActResultRequest(this).startForResult(intent, callback)
}

fun AppCompatActivity.startActivityForResultOk(
    intent: Intent, action: Intent?.() -> Unit
) {
    ActResultRequest(this).startForResult(intent, object : ActivityResultCallback() {
        override fun onActivityResultOk(data: Intent?) {
            action(data)
        }
    })
}

fun Context.requestStoragePermission(callback: () -> Unit) {
    if (this !is BaseActivity) {
        return
    }
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
            if (!Environment.isExternalStorageManager()) {
                AlertDialog.Builder(this).setTitle(R.string.notice)
                    .setMessage(getString(R.string.request_write_sdcard_notice))
                    .setNegativeButton(R.string.cancel, null).setPositiveButton(
                        R.string.ok
                    ) { _, _ ->
                        try {
                            val intent =
                                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                            intent.addCategory("android.intent.category.DEFAULT")
                            intent.data = Uri.parse(String.format("package:%s", packageName))
                            startActivity(intent)
                        } catch (e: Exception) {
                            val intent = Intent()
                            intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                            startActivity(intent)
                        }
                    }.create().show()
            } else {
                callback()
            }
        }

        else -> {
            requestPermission(arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ), object : BaseActivity.PermissionCallback() {
                override fun granted() {
                    callback()
                }
            })
        }
    }
}

fun Context.registerReceiverComp(
    broadcastReceiver: BroadcastReceiver,
    intentFilter: IntentFilter,
    flags: Int = AppCompatActivity.RECEIVER_NOT_EXPORTED
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        registerReceiver(broadcastReceiver, intentFilter, flags)
    } else {
        registerReceiver(broadcastReceiver, intentFilter)
    }
}