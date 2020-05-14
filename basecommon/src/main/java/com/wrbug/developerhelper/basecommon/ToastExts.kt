package com.wrbug.developerhelper.basecommon

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import com.wrbug.developerhelper.commonwidget.flexibletoast.FlexibleToast

fun View.showToast(msg: CharSequence?) {
    Toast.makeText(this.context, msg, Toast.LENGTH_SHORT).show()
}

fun View.showToast(id: Int) {
    Toast.makeText(this.context, id, Toast.LENGTH_SHORT).show()
}


fun Context.showToast(msg: CharSequence?) {
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


fun Activity.showToast(msg: CharSequence) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Activity.showToast(@StringRes id: Int) {
    Toast.makeText(this, id, Toast.LENGTH_SHORT).show()
}


