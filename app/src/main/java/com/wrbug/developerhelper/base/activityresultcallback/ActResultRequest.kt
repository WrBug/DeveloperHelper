package com.wrbug.developerhelper.base.activityresultcallback

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class ActResultRequest {
    private val mFragment: OnActResultEventDispatcherFragment

    constructor(activity: AppCompatActivity) {
        mFragment = getEventDispatchFragment(activity)
    }

    constructor(fragment: Fragment) {
        mFragment = getEventDispatchFragment(fragment)
    }


    private fun getEventDispatchFragment(activity: AppCompatActivity): OnActResultEventDispatcherFragment {
        val fragmentManager = activity.supportFragmentManager
        return this.addFragment(fragmentManager)
    }

    private fun getEventDispatchFragment(fragment: Fragment): OnActResultEventDispatcherFragment {
        val fragmentManager = fragment.childFragmentManager
        return this.addFragment(fragmentManager)
    }

    private fun addFragment(fragmentManager: FragmentManager): OnActResultEventDispatcherFragment {
        var fragment: OnActResultEventDispatcherFragment? =
            this.findEventDispatchFragment(fragmentManager)
        if (fragment == null) {
            fragment = OnActResultEventDispatcherFragment()
            fragmentManager.beginTransaction().add(fragment, "on_act_result_event_dispatcher")
                .commitAllowingStateLoss()
            fragmentManager.executePendingTransactions()
        }

        return fragment
    }

    private fun findEventDispatchFragment(manager: FragmentManager): OnActResultEventDispatcherFragment? {
        return manager.findFragmentByTag("on_act_result_event_dispatcher") as OnActResultEventDispatcherFragment?
    }

    fun startForResult(intent: Intent, callback: ActivityResultCallback) {
        this.mFragment.startForResult(intent, null, callback)
    }

    fun startForResult(intent: Intent, options: Bundle, callback: ActivityResultCallback) {
        this.mFragment.startForResult(intent, options, callback)
    }
}