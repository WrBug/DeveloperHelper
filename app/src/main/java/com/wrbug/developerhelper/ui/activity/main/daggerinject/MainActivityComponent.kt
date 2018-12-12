package com.wrbug.developerhelper.ui.activity.main.daggerinject

import com.wrbug.developerhelper.ui.activity.main.MainActivity
import dagger.Component

@Component
interface MainActivityComponent {
    fun inject(activity: MainActivity)
}