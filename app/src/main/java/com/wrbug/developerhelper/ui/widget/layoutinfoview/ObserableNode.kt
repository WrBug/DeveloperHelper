package com.wrbug.developerhelper.ui.widget.layoutinfoview

import de.blox.graphview.Node
import de.blox.graphview.Vector

class ObserableNode(data: Any?) : Node(data) {
    private var listener: OnPosChangedListener? = null
    override fun setPos(pos: Vector?) {
        super.setPos(pos)
        listener?.onChanged(pos)
    }

    fun setOnPosChangedListener(onPosChangedListener: OnPosChangedListener) {
        listener = onPosChangedListener
    }

    interface OnPosChangedListener {
        fun onChanged(pos: Vector?)
    }
}