package com.wrbug.developerhelper.ui.activity.hierachy

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.commonutil.entity.ApkInfo
import com.wrbug.developerhelper.commonutil.UiUtils
import com.wrbug.developerhelper.commonutil.dp2px
import com.wrbug.developerhelper.databinding.DialogApkInfoBinding
import com.wrbug.developerhelper.util.isPortrait
import io.reactivex.rxjava3.disposables.CompositeDisposable

class AppInfoDialog : DialogFragment() {

    private val apkInfo: ApkInfo? by lazy {
        arguments?.getParcelable("apkInfo")
    }
    private var listener: AppInfoDialogEventListener? = null
    private lateinit var binding: DialogApkInfoBinding
    private lateinit var disposable: CompositeDisposable
    private val pagerAdapter by lazy {
        AppInfoPagerAdapter(this, disposable)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (activity is AppInfoDialogEventListener) {
            listener = activity
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        disposable = CompositeDisposable()
        binding = DialogApkInfoBinding.inflate(inflater, container, false)
        dialog?.window?.run {
            val layoutParams = attributes
            if (isPortrait()) {
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                layoutParams.height = UiUtils.getDeviceHeight() / 2 + dp2px(40F)
                setGravity(Gravity.TOP)
            } else {
                layoutParams.width = UiUtils.getDeviceWidth() / 2
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                setGravity(Gravity.END)
            }
            attributes = layoutParams
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.titleContainer.setPadding(0, UiUtils.getStatusHeight(), 0, 0)
        pagerAdapter.listener = listener
        binding.viewPager.adapter = pagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        apkInfo?.let {
            binding.logoIv.setImageDrawable(it.getIco())
            binding.titleTv.text = it.getAppName()
            binding.subTitleTv.text = it.applicationInfo.packageName
        }
        pagerAdapter.loadData(apkInfo)
    }

    override fun onDestroyView() {
        listener?.close()
        disposable.dispose()
        super.onDestroyView()
    }
}