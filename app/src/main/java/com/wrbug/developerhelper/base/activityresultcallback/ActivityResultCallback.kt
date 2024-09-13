package com.wrbug.developerhelper.base.activityresultcallback

import android.app.Activity
import android.content.Intent

abstract class ActivityResultCallback {

    fun dispatchActivityResult(resultCode: Int, data: Intent?) {
        onActivityResult(resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            onActivityResultOk(data)
        }
    }

    protected open fun onActivityResult(resultCode: Int, data: Intent?) {

    }

    protected open fun onActivityResultOk(data: Intent?) {

    }
}
