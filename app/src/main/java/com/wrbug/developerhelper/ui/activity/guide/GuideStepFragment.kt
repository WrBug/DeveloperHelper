package com.wrbug.developerhelper.ui.activity.guide

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.basecommon.BaseFragment
import kotlinx.android.synthetic.main.fragment_guide.view.*


 abstract class GuideStepFragment : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_guide, container, false)
        rootView.labelTv.text = getLabelText()
        return rootView
    }
    abstract fun getLabelText(): String
}