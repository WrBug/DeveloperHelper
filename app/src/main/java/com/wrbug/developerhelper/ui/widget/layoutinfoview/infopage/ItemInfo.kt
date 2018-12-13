package com.wrbug.developerhelper.ui.widget.layoutinfoview.infopage

import com.wrbug.developerhelper.generated.callback.OnClickListener

class ItemInfo(var title: String = "", var content: Any = "") {
    var clickListener: OnClickListener? = null
        private set(value) {
            field = value
        }

    fun setOnClickListener(listener: OnClickListener) {
        clickListener = listener
    }
}
