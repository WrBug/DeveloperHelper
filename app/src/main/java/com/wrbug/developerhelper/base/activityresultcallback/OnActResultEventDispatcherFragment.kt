package com.wrbug.developerhelper.base.activityresultcallback

import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import androidx.fragment.app.Fragment

class OnActResultEventDispatcherFragment : Fragment() {
    val TAG = "on_act_result_event_dispatcher"
    private val mCallbacks = SparseArray<ActivityResultCallback>()

    fun startForResult(intent: Intent, options: Bundle?, callback: ActivityResultCallback) {
        val requestCode = callback.hashCode() and 0xffff
        this.mCallbacks.put(requestCode, callback)
        this.startActivityForResult(intent, requestCode, options)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val callback = this.mCallbacks.get(requestCode) as ActivityResultCallback
        this.mCallbacks.remove(requestCode)
        callback.dispatchActivityResult(resultCode, data)
    }
}