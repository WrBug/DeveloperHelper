package com.wrbug.developerhelper.basecommon

import androidx.annotation.StringRes

//fun Activity.showToast(msg: CharSequence?) {
//    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
//
//}


fun showToast(msg: CharSequence) {
    BaseApp.instance.showToast(msg.toString())
}

fun showToast(@StringRes id: Int) {
    BaseApp.instance.showToast(id)
}