package com.wrbug.developerhelper.ui.activity.guide

import android.animation.ArgbEvaluator
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.basecommon.BaseActivity
import com.wrbug.developerhelper.databinding.ActivityDatabaseEditBinding
import com.wrbug.developerhelper.databinding.ActivityGuideBinding

class GuideActivity: BaseActivity() {

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private lateinit var binding: ActivityGuideBinding
    private val bgColors: IntArray by lazy {
        intArrayOf(
            ContextCompat.getColor(this, R.color.colorPrimary),
            ContextCompat.getColor(this, R.color.cyan_500),
            ContextCompat.getColor(this, R.color.light_blue_500)
        )
    }
    private var currentPosition = 0
    private var indicators: Array<View>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuideBinding.inflate(layoutInflater).inject()
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        binding.viewPager.adapter = mSectionsPagerAdapter
        binding.viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                currentPosition = position
                updateIndicators(position)
                binding.viewPager.setBackgroundColor(bgColors[position])
                binding.buttonPre.visibility = if (position == 0) View.GONE else View.VISIBLE
                binding.buttonNext.visibility = if (position == 2) View.GONE else View.VISIBLE
                binding.buttonFinish.visibility = if (position == 2) View.VISIBLE else View.GONE
            }

            override fun onPageScrollStateChanged(position: Int) {

            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                val colorUpdate = ArgbEvaluator().evaluate(
                    p1,
                    bgColors[p0],
                    bgColors[if (p0 == 2) p0 else p0 + 1]
                ) as Int
                binding.viewPager.setBackgroundColor(colorUpdate)
            }

        })
        indicators = arrayOf(
            binding.imageViewIndicator0 as View,
            binding.imageViewIndicator1 as View,
            binding.imageViewIndicator2 as View
        )
    }

    private fun updateIndicators(position: Int) {
        for (i in 0 until indicators?.size!!) {
            indicators?.get(i)?.setBackgroundResource(
                if (i == position) R.drawable.onboarding_indicator_selected else R.drawable.onboarding_indicator_unselected
            )
        }
    }
}
