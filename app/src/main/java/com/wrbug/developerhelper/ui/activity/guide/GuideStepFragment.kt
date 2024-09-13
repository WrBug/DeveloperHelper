package com.wrbug.developerhelper.ui.activity.guide

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wrbug.developerhelper.base.BaseFragment
import com.wrbug.developerhelper.databinding.FragmentGuideBinding

abstract class GuideStepFragment: BaseFragment() {

    protected lateinit var binding: FragmentGuideBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGuideBinding.inflate(inflater, container, false)
        binding.labelTv.text = getLabelText()
        return binding.root
    }

    abstract fun getLabelText(): String
}