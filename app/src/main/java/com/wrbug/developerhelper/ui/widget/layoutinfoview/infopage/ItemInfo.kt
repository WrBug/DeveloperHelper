package com.wrbug.developerhelper.ui.widget.layoutinfoview.infopage

import android.view.View

class ItemInfo @JvmOverloads constructor(
    var title: String = "",
    var content: Any = "",
    var showCopy: Boolean = true,
    listener: View.OnClickListener? = null
) {
    var id = title
    var textColor: Int = 0x8a000000.toInt()
    var clickListener: View.OnClickListener? = listener
        private set

    fun setOnClickListener(listener: View.OnClickListener) {
        clickListener = listener
    }

    fun setOnClickListener(onclick: View?.() -> Unit) {
        setOnClickListener(View.OnClickListener { v -> v.onclick() })
    }
}
