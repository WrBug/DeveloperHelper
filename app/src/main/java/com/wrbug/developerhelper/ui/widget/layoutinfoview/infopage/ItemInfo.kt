package com.wrbug.developerhelper.ui.widget.layoutinfoview.infopage

import android.view.View

class ItemInfo(var title: String = "", var content: Any = "") {
    var clickListener: View.OnClickListener? = null
        private set(value) {
            field = value
        }

    fun setOnClickListener(listener: View.OnClickListener) {
        clickListener = listener
    }
}
