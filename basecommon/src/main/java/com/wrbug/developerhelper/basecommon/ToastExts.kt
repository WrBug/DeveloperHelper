package com.wrbug.developerhelper.basecommon

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.annotation.IdRes

fun Context.showToast(msg: CharSequence?) =
    Toast.makeText(if (this is Activity) this else applicationContext, msg, Toast.LENGTH_SHORT).show()


@SuppressLint("ResourceType")
fun Context.showToast(@IdRes id: Int) =
    Toast.makeText(if (this is Activity) this else applicationContext, id, Toast.LENGTH_SHORT).show()