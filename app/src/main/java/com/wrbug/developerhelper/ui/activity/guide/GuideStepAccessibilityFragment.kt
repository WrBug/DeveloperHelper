package com.wrbug.developerhelper.ui.activity.guide

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.service.DeveloperHelperAccessibilityService
import kotlinx.android.synthetic.main.fragment_guide.*

class GuideStepAccessibilityFragment : GuideStepFragment() {
    override fun getLabelText(): String {
        return "无障碍功能"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        checkIsOpen()
        icoIv.setImageResource(R.drawable.ic_accessibility_black)
        contentTv.setOnClickListener {
            if (DeveloperHelperAccessibilityService.serviceRunning) {
                return@setOnClickListener
            }
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            activity?.startActivity(intent)

        }
    }

    private fun checkIsOpen() {
        contentTv.text =
                if (DeveloperHelperAccessibilityService.serviceRunning) "无障碍辅助已开启" else "无障碍辅助已关闭，将无法分析布局和页面信息，点击开启"
    }

    override fun onResume() {
        super.onResume()
        checkIsOpen()
    }

    companion object {
        fun instance(): GuideStepAccessibilityFragment {
            val fragment = GuideStepAccessibilityFragment()
            return fragment
        }
    }
}