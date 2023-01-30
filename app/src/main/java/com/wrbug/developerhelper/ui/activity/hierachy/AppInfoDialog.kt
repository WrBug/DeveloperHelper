package com.wrbug.developerhelper.ui.activity.hierachy

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.commonutil.entity.ApkInfo
import com.wrbug.developerhelper.commonutil.entity.TopActivityInfo
import com.wrbug.developerhelper.commonutil.UiUtils
import com.wrbug.developerhelper.commonutil.dp2px
import com.wrbug.developerhelper.databinding.DialogApkInfoBinding

class AppInfoDialog: DialogFragment() {

    private var apkInfo: ApkInfo? = null
    private var topActivity: TopActivityInfo? = null
    private var listener: AppInfoDialogEventListener? = null
    private lateinit var binding: DialogApkInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog)
        arguments?.let {
            apkInfo = it.getParcelable("apkInfo")
            topActivity = it.getParcelable("topActivity")
        }
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (activity is AppInfoDialogEventListener) {
            listener = activity
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogApkInfoBinding.inflate(inflater, container, false)
        dialog?.window?.run {
            val layoutParams = attributes
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = UiUtils.getDeviceHeight() / 2 + dp2px(40F)
            attributes = layoutParams
            setGravity(Gravity.TOP)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.titleContainer.setPadding(0, UiUtils.getStatusHeight(), 0, 0)
        activity?.let {
            val pagerAdapter = AppInfoPagerAdapter(this, apkInfo, topActivity)
            pagerAdapter.listener = listener
            binding.viewPager.adapter = pagerAdapter
            binding.tabLayout.setupWithViewPager(binding.viewPager)
        }
        apkInfo?.let { it ->
            binding.logoIv.setImageDrawable(it.getIco())
            binding.titleTv.text = it.getAppName()
            binding.subTitleTv.text = it.applicationInfo.packageName
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener?.close()
    }
}