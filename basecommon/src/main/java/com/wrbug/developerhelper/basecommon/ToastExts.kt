package com.wrbug.developerhelper.basecommon

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.annotation.StringRes
import com.wrbug.developerhelper.commonwidget.flexibletoast.FlexibleToast

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

fun Application.showToast(msg: CharSequence) {
    val appHandler = Handler()
    val flexibleToast = FlexibleToast(this)
    val builder = FlexibleToast.Builder(this).setGravity(FlexibleToast.GRAVITY_BOTTOM)
    builder.setSecondText(msg.toString())
    if (Looper.myLooper() !== Looper.getMainLooper()) {
        appHandler.post { flexibleToast.toastShow(builder) }
    } else {
        flexibleToast.toastShow(builder)
    }
}
