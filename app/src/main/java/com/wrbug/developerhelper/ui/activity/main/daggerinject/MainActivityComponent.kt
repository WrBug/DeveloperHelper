package com.wrbug.developerhelper.basecommon.daggerinject

import com.wrbug.developerhelper.basecommon.BaseActivity
import com.wrbug.developerhelper.ui.activity.main.MainActivity
import dagger.Component

@Component
interface MainActivityComponent {
    fun inject(activity: MainActivity)
}