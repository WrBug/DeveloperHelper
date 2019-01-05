package com.wrbug.developerhelper.basecommon

import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import java.util.ArrayList

abstract class BaseActivity : AppCompatActivity() {
    private lateinit var toastRootView: View
    protected lateinit var context: BaseActivity
    private var mPermissionCallback: PermissionCallback? = null

    companion object {
        private const val PERMISSION_REQUEST_CODE = 0xAADF1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        context = this
        super.onCreate(savedInstanceState)
    }

    override fun setContentView(layoutResID: Int) {
        toastRootView = layoutInflater.inflate(layoutResID, null)
        setContentView(toastRootView)
    }

    fun showSnack(msg: String) {
        Snackbar.make(toastRootView, msg, Snackbar.LENGTH_SHORT).show()
    }

    fun showSnack(id: Int) {
        Snackbar.make(toastRootView, id, Snackbar.LENGTH_SHORT).show()
    }


    @TargetApi(Build.VERSION_CODES.M)
    fun requestPermission(permissions: Array<String>, callback: PermissionCallback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            callback.granted()
            return
        }
        val list = ArrayList<String>()
        for (permission in permissions) {
            val hasPermission = checkSelfPermission(permission)
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                list.add(permission)
            }
        }
        if (list.isEmpty()) {
            callback.granted()
            return
        }
        mPermissionCallback = callback
        requestPermissions(list.toTypedArray(), PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val list = ArrayList<String>()
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    list.add(permissions[i])
                }
            }
            if (list.isEmpty()) {
                mPermissionCallback?.granted()
                return
            }
            mPermissionCallback?.denied(list)
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    abstract class PermissionCallback {
        abstract fun granted()

        open fun denied(permissions: List<String>) {

        }
    }
}