package com.wrbug.developerhelper.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.provider.Settings
import android.view.LayoutInflater
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.constant.ReceiverConstant
import com.yhao.floatwindow.FloatWindow
import com.yhao.floatwindow.Screen


class FloatWindowService : Service() {

    companion object {
        const val FLOAT_BUTTON = "floatButton"
        fun start(context: Context) {
            context.startService(Intent(context, FloatWindowService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FloatWindowService::class.java))
        }
    }


    private val receiver = Receiver()
    override fun onCreate() {
        super.onCreate()
        initReceiver()
        LayoutInflater.from(this).inflate(R.layout.layout_float_window_button, null)?.let { it ->
            it.setOnClickListener {
                if (!DeveloperHelperAccessibilityService.serviceRunning) {
                    if (AccessibilityManager.startService(this)) {
                        it.postDelayed({
                            sendBroadcast(Intent(ReceiverConstant.ACTION_HIERARCHY_VIEW))
                        }, 500)
                    }
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
                .setTag(FLOAT_BUTTON)
                .setDesktopShow(true)                        //桌面显示
                .build()
        }

    }

    private fun showFloatButton() {
        FloatWindow.get(FLOAT_BUTTON).show()
    }

    private fun hideFloatButton() {
        FloatWindow.get(FLOAT_BUTTON).hide()
    }

    private fun initReceiver() {
        val filter = IntentFilter(ReceiverConstant.ACTION_SET_FLOAT_BUTTON_VISIBLE)
        registerReceiver(receiver, filter)
    }

    @SuppressLint("WrongConstant")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showFloatButton()
        return super.onStartCommand(intent, START_STICKY, startId)
    }

    override fun onDestroy() {
        FloatWindow.destroy(FLOAT_BUTTON)
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder = null!!


    private inner class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ReceiverConstant.ACTION_SET_FLOAT_BUTTON_VISIBLE -> {
                    val visible = intent.getBooleanExtra("visible", false)
                    if (visible) {
                        showFloatButton()
                    } else {
                        hideFloatButton()
                    }
                }
            }
        }

    }
}
