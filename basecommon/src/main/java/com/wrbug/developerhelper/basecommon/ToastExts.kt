package com.wrbug.developerhelper.basecommon

import android.app.Activity
import android.widget.Toast

//fun Activity.showToast(msg: CharSequence?) {
//    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
//
//}


fun showToast(msg: CharSequence) {
    BaseApp.instance.showToast(msg.toString())
}