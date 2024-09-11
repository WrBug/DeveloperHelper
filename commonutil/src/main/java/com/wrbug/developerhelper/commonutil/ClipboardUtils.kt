package com.wrbug.developerhelper.commonutil

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils
import android.content.Context.CLIPBOARD_SERVICE

/**
 * ClipboardUtils
 *
 */
object ClipboardUtils {

    fun getClipboardText(context: Context): String {
        try {
            val cm = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val cd2 = cm.primaryClip ?: return ""
            val itemAt = cd2.getItemAt(0) ?: return ""
            val text = itemAt.text
            return if (TextUtils.isEmpty(text)) {
                ""
            } else text.toString()
        } catch (t: Throwable) {
        }

        return ""
    }

    fun saveClipboardText(context: Context, text: String) {
        try {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // 将文本内容放到系统剪贴板里。
            cm.setPrimaryClip(ClipData.newPlainText(null, text))
        } catch (t: Throwable) {
        }

    }
}
