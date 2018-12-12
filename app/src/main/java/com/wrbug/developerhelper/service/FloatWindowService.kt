package com.wrbug.developerhelper.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.constant.ReceiverConstant
import com.yhao.floatwindow.FloatWindow
import com.yhao.floatwindow.Screen


class FloatWindowService : Service() {


    companion object {
        const val tag = "floatView"
        fun start(context: Context) {
            context.startService(Intent(context, FloatWindowService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FloatWindowService::class.java))
        }
    }

    private val mWindowManager: WindowManager by lazy {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager
    }
    private val receiver = Receiver()
    private var floatView: View? = null
    override fun onCreate() {
        super.onCreate()
        initReceiver()
        floatView = LayoutInflater.from(this).inflate(R.layout.layout_float_window_button, null)
        floatView?.let { it ->
            it.setOnClickListener {
                if (!DeveloperHelperAccessibilityService.serviceRunning) {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    return@setOnClickListener
                }
                sendBroadcast(Intent(ReceiverConstant.ACTION_HIERARCHY_VIEW))
            }
            FloatWindow
                .with(applicationContext)
                .setView(it)
                .setWidth(Screen.width, 0.1f)                               //设置控件宽高
                .setHeight(Screen.width, 0.1f)
                .setY(Screen.height, 0.3f)
                .setTag(tag)
                .setDesktopShow(true)                        //桌面显示
                .build()
            showFloatView()
        }


    }

    private fun showFloatView() {
        FloatWindow.get(tag).show()
    }

    private fun hideFloatView() {
        FloatWindow.get(tag).hide()
    }

    private fun initReceiver() {
        val filter = IntentFilter(ReceiverConstant.ACTION_SET_FLOAT_VIEW_VISIBLE)
        registerReceiver(receiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        mWindowManager.removeView(floatView)
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder = null!!


    private inner class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ReceiverConstant.ACTION_SET_FLOAT_VIEW_VISIBLE -> {
                    val visible = intent.getBooleanExtra("visible", false)
                    if (visible) {
                        showFloatView()
                    } else {
                        hideFloatView()
                    }
                }
            }
        }

    }
}
