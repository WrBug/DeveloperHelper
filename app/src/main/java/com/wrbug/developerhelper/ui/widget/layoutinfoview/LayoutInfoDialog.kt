package com.wrbug.developerhelper.ui.widget.layoutinfoview

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.base.ExtraKey
import com.wrbug.developerhelper.base.entry.HierarchyNode
import com.wrbug.developerhelper.commonutil.UiUtils
import com.wrbug.developerhelper.commonutil.dp2px
import com.wrbug.developerhelper.databinding.ViewLayoutInfoBinding
import com.wrbug.developerhelper.util.isPortrait

class LayoutInfoDialog : DialogFragment() {

    companion object {
        fun show(
            fragmentManager: FragmentManager,
            list: List<HierarchyNode>?,
            selectedNode: HierarchyNode,
            listener: ((HierarchyNode, HierarchyNode?) -> Unit)
        ) {
            LayoutInfoDialog().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ExtraKey.DATA, list?.let { ArrayList(it) })
                    putParcelable(ExtraKey.SELECTED, selectedNode)
                }
                onNodeChangedListener = listener
            }.show(fragmentManager, "LayoutInfoDialog")
        }
    }

    private val nodeList: List<HierarchyNode>? by lazy {
        arguments?.getParcelableArrayList(ExtraKey.DATA)
    }
    private val hierarchyNode: HierarchyNode? by lazy {
        arguments?.getParcelable(ExtraKey.SELECTED)
    }
    private var onNodeChangedListener: ((HierarchyNode, HierarchyNode?) -> Unit)? = null

    val adapter by lazy {
        LayoutInfoViewPagerAdapter(requireContext(), nodeList, hierarchyNode!!)
    }

    private lateinit var binding: ViewLayoutInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ViewLayoutInfoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setCanceledOnTouchOutside(true)
            window?.run {
                val layoutParams = attributes
                if (isPortrait()) {
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    layoutParams.height = UiUtils.getDeviceHeight() / 2
                    setGravity(Gravity.BOTTOM)
                } else {
                    layoutParams.width = UiUtils.getDeviceWidth() / 2
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    setGravity(Gravity.END)
                }
                attributes = layoutParams
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (dialog?.window?.attributes?.gravity != Gravity.BOTTOM) {
            binding.layoutInfoContainer.setPadding(0, UiUtils.getStatusHeight(), 0, 0)
        }
        onNodeChangedListener?.let {
            adapter.setOnNodeChangedListener(it)
        }
        initViewpager()
    }

    private fun initViewpager() {
        binding.viewPager.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }
}
