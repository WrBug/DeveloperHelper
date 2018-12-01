package com.wrbug.developerhelper.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.constant.ReceiverConstant


class FloatWindowService : Service() {
    private val mWindowManager: WindowManager by lazy {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager
    }
    private var floatView: View? = null
    override fun onCreate() {
        super.onCreate()
        //设置悬浮窗布局属性
        val mWindowLayoutParams = WindowManager.LayoutParams();
        //设置类型,具体有哪些值可取在后面附上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mWindowLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mWindowLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        //设置行为选项,具体有哪些值可取在后面附上
        mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //设置悬浮窗的显示位置
        mWindowLayoutParams.gravity = Gravity.LEFT or Gravity.TOP;
//        //设置悬浮窗的横竖屏,会影响屏幕方向,只要悬浮窗不消失,屏幕方向就会一直保持,可以强制屏幕横屏或竖屏
//        mWindowLayoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        //设置 x 轴的偏移量
        mWindowLayoutParams.x = 0;
        //设置 y 轴的偏移量
        mWindowLayoutParams.y = 0;
        //如果悬浮窗图片为透明图片,需要设置该参数为 PixelFormat.RGBA_8888
        mWindowLayoutParams.format = PixelFormat.RGBA_8888;
        //设置悬浮窗的宽度
        mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        //设置悬浮窗的高度
        mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        floatView = LayoutInflater.from(this).inflate(R.layout.layout_float_window_button, null)
        floatView?.setOnClickListener {
            sendBroadcast(Intent(ReceiverConstant.ACTION_HIERARCHY_VIEW))
        }
        mWindowManager.addView(floatView, mWindowLayoutParams)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        mWindowManager.removeView(floatView)
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        return null!!
    }
}
