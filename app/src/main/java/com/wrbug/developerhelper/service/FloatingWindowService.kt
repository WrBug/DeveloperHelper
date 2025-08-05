package com.wrbug.developerhelper.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.base.isServiceRunning
import com.wrbug.developerhelper.base.registerReceiverComp
import com.wrbug.developerhelper.commonutil.UiUtils
import com.wrbug.developerhelper.commonutil.addTo
import com.wrbug.developerhelper.commonutil.dpInt
import com.wrbug.developerhelper.commonutil.shell.ShellManager
import com.wrbug.developerhelper.constant.ReceiverConstant
import com.wrbug.developerhelper.ui.activity.main.MainActivity
import com.wrbug.developerhelper.util.isPortrait
import com.wrbug.developerhelper.util.setOnDoubleCheckClickListener
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlin.math.abs

class FloatingWindowService : Service() {
    companion object {
        private const val CHANNEL_ID = "FLOAT_WINDOW_DEMON"
        fun start(context: Context) {
            if (isServiceRunning(context, FloatingWindowService::class.java)) {
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, FloatingWindowService::class.java))
            } else {
                context.startService(Intent(context, FloatingWindowService::class.java))
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FloatingWindowService::class.java))
        }

        fun setFloatButtonVisible(context: Context, visible: Boolean) {
            val intent = Intent(ReceiverConstant.ACTION_SET_FLOAT_BUTTON_VISIBLE)
            intent.setPackage(context.packageName)
            intent.putExtra("visible", visible)
            context.sendBroadcast(intent)
        }
    }

    private lateinit var windowManager: WindowManager
    private lateinit var floatingButton: View
    private lateinit var notification: Notification
    private lateinit var params: WindowManager.LayoutParams
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private var screenWidth = 0
    private var screenHeight = 0
    private lateinit var disposable: CompositeDisposable
    private lateinit var receiver: Receiver

    private val floatCustomView: RemoteViews by lazy {
        RemoteViews(packageName, R.layout.view_float_custom).apply {
            setOnClickPendingIntent(
                R.id.adbWifiContainer, PendingIntent.getBroadcast(
                    applicationContext,
                    0,
                    Intent(ReceiverConstant.ACTION_ADB_WIFI_CLICKED),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        disposable = CompositeDisposable()
        initReceiver()
        initScreenDimensions()
        createFloatingButton()
        initNotification()
    }

    private fun initReceiver() {
        receiver = Receiver()
        val filter = IntentFilter(ReceiverConstant.ACTION_SET_FLOAT_BUTTON_VISIBLE)
        filter.addAction(ReceiverConstant.ACTION_ADB_WIFI_CLICKED)
        registerReceiverComp(receiver, filter)
    }


    private fun initScreenDimensions() {
        val display = (getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay
        val size = Point()
        display.getSize(size)
        screenWidth = size.x
        screenHeight = size.y
    }

    private fun createFloatingButton() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val screen = if (isPortrait()) {
            UiUtils.getDeviceWidth() * 0.1
        } else {
            UiUtils.getDeviceHeight() * 0.1
        }.toInt() + 20.dpInt()
        floatingButton =
            LayoutInflater.from(this).inflate(R.layout.layout_float_window_button, null).apply {
                // 设置按钮大小
                layoutParams = ViewGroup.LayoutParams(screen, screen)
                setPadding(10.dpInt())
            }
        params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                screen,
                screen,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                screen,
                screen,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        }

        // 初始位置 - 右上角
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = screenHeight / 2

        windowManager.addView(floatingButton, params)
        setupTouchEvents()
        setupClickEvent()
    }

    private fun setupTouchEvents() {
        floatingButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()

                    // 限制按钮不超出屏幕
                    params.x = params.x.coerceIn(0, screenWidth - floatingButton.width)
                    params.y = params.y.coerceIn(0, screenHeight - floatingButton.height)

                    windowManager.updateViewLayout(floatingButton, params)
                    val moveThreshold = ViewConfiguration.get(this).scaledTouchSlop
                    if (abs(event.rawX - initialTouchX) > moveThreshold || abs(event.rawY - initialTouchY) > moveThreshold) {
                        isDragging = true
                    }

                    if (isDragging) {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(floatingButton, params)
                        true
                    } else {
                        false
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        v.performClick()
                        true // 让点击事件处理
                    } else {
                        snapToNearestEdge()
                        true
                    }
                }

                else -> false
            }
        }
    }

    private fun snapToNearestEdge() {
        val buttonCenterX = params.x + floatingButton.width / 2
        val buttonCenterY = params.y + floatingButton.height / 2

        // 计算到各边的距离
        val distanceToLeft = buttonCenterX
        val distanceToRight = screenWidth - buttonCenterX
        val distanceToTop = buttonCenterY
        val distanceToBottom = screenHeight - buttonCenterY

        // 找到最小距离
        val minHorizontal = minOf(distanceToLeft, distanceToRight)
        val minVertical = minOf(distanceToTop, distanceToBottom)

        // 决定吸附到哪边
        if (minHorizontal < minVertical) {
            // 水平方向吸附
            params.x =
                if (distanceToLeft < distanceToRight) 0 else screenWidth - floatingButton.width
        } else {
            // 垂直方向吸附
            params.y =
                if (distanceToTop < distanceToBottom) 0 else screenHeight - floatingButton.height
        }

        windowManager.updateViewLayout(floatingButton, params)
    }

    private fun setupClickEvent() {
        floatingButton.setOnDoubleCheckClickListener {
            if (!DeveloperHelperAccessibilityService.isAccessibilitySettingsOn()) {
                AccessibilityManager.startService(this).subscribe({ data ->
                    if (data) {
                        it.postDelayed({
                            sendBroadcast(
                                Intent(ReceiverConstant.ACTION_HIERARCHY_VIEW).setPackage(
                                    packageName
                                )
                            )
                        }, 500)
                    }
                }, {

                }).addTo(disposable)
                return@setOnDoubleCheckClickListener
            }
            sendBroadcast(Intent(ReceiverConstant.ACTION_HIERARCHY_VIEW).setPackage(packageName))
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun initNotification() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, getString(R.string.demon_process), NotificationManager.IMPORTANCE_LOW
            )
            channel.enableLights(true)
            channel.setShowBadge(true)
            notificationManager.createNotificationChannel(channel)
        }
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        }
        val builder = NotificationCompat.Builder(this, CHANNEL_ID).setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.demon_process_content))
            .setSmallIcon(R.drawable.ic_launcher_notify).setVibrate(null)
        notification = builder.build()
        notification.flags =
            Notification.FLAG_ONGOING_EVENT or Notification.FLAG_NO_CLEAR or Notification.FLAG_FOREGROUND_SERVICE
        updateNotification()
    }


    private fun updateNotification() {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        } else {
            0
        }
        runCatching {
            ServiceCompat.startForeground(this, 0x10000, notification, type)
        }.getOrElse {
            it.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
        if (::receiver.isInitialized) {
            unregisterReceiver(receiver)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
        if (::floatingButton.isInitialized) {
            windowManager.removeView(floatingButton)
        }
    }

    private fun setFloatButtonVisible(isVisible: Boolean) {
        floatingButton.isVisible = isVisible
    }

    private fun updateNotificationContent(text: String) {
        floatCustomView.setTextViewText(R.id.contentTv, text)
        updateNotification()
    }

    private fun updateNotificationWifi(id: Int) {
        floatCustomView.setImageViewResource(R.id.adbWifiIv, id)
        updateNotification()
    }

    private inner class Receiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ReceiverConstant.ACTION_SET_FLOAT_BUTTON_VISIBLE -> {
                    val visible = intent.getBooleanExtra("visible", false)
                    setFloatButtonVisible(visible)
                }

                ReceiverConstant.ACTION_ADB_WIFI_CLICKED -> {
                    updateNotificationContent("正在开启adb wifi")
                    val success = ShellManager.openAdbWifi()
                    if (success) {
                        updateNotificationContent("adb wifi 已开启")
                        updateNotificationWifi(R.drawable.ic_wifi_primary)
                    } else {
                        updateNotificationContent("adb wifi 开启失败")
                        updateNotificationWifi(R.drawable.ic_wifi_gray)
                    }
                }
            }
        }

    }


}