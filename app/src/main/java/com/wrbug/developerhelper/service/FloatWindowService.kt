package com.wrbug.developerhelper.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.constant.ReceiverConstant
import com.wrbug.developerhelper.ui.widget.layoutinfoview.LayoutInfoView


class FloatWindowService : Service() {
    private val mWindowManager: WindowManager by lazy {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager
    }
    private val receiver = Receiver()
    private var floatView: View? = null
    override fun onCreate() {
        super.onCreate()
        initReceiver()
        //设置悬浮窗布局属性
        val mWindowLayoutParams = WindowManager.LayoutParams()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mWindowLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            mWindowLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        mWindowLayoutParams.gravity = Gravity.LEFT or Gravity.TOP
        mWindowLayoutParams.x = 0
        mWindowLayoutParams.y = 0
        mWindowLayoutParams.format = PixelFormat.RGBA_8888
        mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        floatView = LayoutInflater.from(this).inflate(R.layout.layout_float_window_button, null)
        floatView?.setOnClickListener {
            sendBroadcast(Intent(ReceiverConstant.ACTION_HIERARCHY_VIEW))
        }
        mWindowManager.addView(floatView, mWindowLayoutParams)
    }

    private fun initReceiver() {
        val filter = IntentFilter(ReceiverConstant.ACTION_SET_FLOAT_VIEW_VISIBLE)
        filter.addAction(ReceiverConstant.ACTION_SHOW_LAYOUT_INFO_VIEW)
        filter.addAction(ReceiverConstant.ACTION_HIDE_LAYOUT_INFO_VIEW)
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

    override fun onBind(intent: Intent): IBinder {
        return null!!
    }


    private inner class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ReceiverConstant.ACTION_SET_FLOAT_VIEW_VISIBLE -> {
                    val visible = intent.getBooleanExtra("visible", false)
                    floatView?.visibility = if (visible) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
            }
        }

    }
}
