package com.wrbug.developerhelper.base

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import com.wrbug.developerhelper.ui.widget.flexibletoast.FlexibleToast

fun View.showToast(msg: CharSequence?) {
    Toast.makeText(this.context, msg, Toast.LENGTH_SHORT).show()
}

fun View.showToast(id: Int) {
    Toast.makeText(this.context, id, Toast.LENGTH_SHORT).show()
}


fun Context.showToast(msg: CharSequence?) {
    FlexibleToast.toastShow(this, msg.toString())
}


fun Activity.showToast(msg: CharSequence) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Activity.showToast(@StringRes id: Int) {
    Toast.makeText(this, id, Toast.LENGTH_SHORT).show()
}


