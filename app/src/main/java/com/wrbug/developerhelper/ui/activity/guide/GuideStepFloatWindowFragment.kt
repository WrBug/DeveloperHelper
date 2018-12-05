package com.wrbug.developerhelper.ui.activity.guide

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.service.FloatWindowService
import com.wrbug.developerhelper.util.DeviceUtils
import kotlinx.android.synthetic.main.fragment_guide.*

class GuideStepFloatWindowFragment : GuideStepFragment() {
    override fun getLabelText(): String {
        return getString(R.string.float_window_permission)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        icoIv.setImageResource(R.drawable.ic_float_air_bubble)
        contentTv.setOnClickListener {
            if (DeviceUtils.isFloatWindowOpened(activity!!)) {
                return@setOnClickListener
            }
            startActivityForResult(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${activity?.packageName}")),
                0
            )
        }
    }

    override fun onResume() {
        super.onResume()
        checkFloatWindow()
    }

    private fun checkFloatWindow() {
        if (activity != null && DeviceUtils.isFloatWindowOpened(activity!!)) {
            contentTv.text = getString(R.string.float_window_opened)
            activity?.startService(Intent(activity, FloatWindowService::class.java))
        } else {
            contentTv.text = getString(R.string.float_window_closed)
        }
    }

    companion object {
        fun instance(): GuideStepFloatWindowFragment {
            val fragment = GuideStepFloatWindowFragment()
            return fragment
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 0) {
            checkFloatWindow()
        }
    }
}