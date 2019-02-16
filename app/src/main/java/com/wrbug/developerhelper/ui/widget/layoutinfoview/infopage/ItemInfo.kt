package com.wrbug.developerhelper.ui.widget.layoutinfoview.infopage

import android.view.View

class ItemInfo(var title: String = "", var content: Any = "") {
    var textColor: Int = 0x8a000000.toInt()
    var clickListener: View.OnClickListener? = null
        private set(value) {
            field = value
        }

    fun setOnClickListener(listener: View.OnClickListener) {
        clickListener = listener
    }
}
