package com.wrbug.developerhelper.ui.widget.appdatainfoview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.commonutil.ApkUtils
import com.wrbug.developerhelper.commonutil.AppInfoManager
import com.wrbug.developerhelper.commonutil.entity.ApkInfo
import com.wrbug.developerhelper.commonutil.shell.ShellManager
import com.wrbug.developerhelper.ui.activity.databaseedit.DatabaseEditActivity
import com.wrbug.developerhelper.ui.activity.sharedpreferencesedit.SharedPreferenceEditActivity
import com.wrbug.developerhelper.commonutil.UiUtils
import com.wrbug.developerhelper.databinding.ViewAppDataInfoBinding
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File

class AppDataInfoView: FrameLayout {

    private lateinit var binding: ViewAppDataInfoBinding
    var apkInfo: ApkInfo? = null
        set(value) {
            field = value
            loadData()
        }

    constructor(context: Context): super(context) {
        initView()

    }

    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet) {
        initView()
    }

    private fun initView() {
        binding = ViewAppDataInfoBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private fun loadData() {
        apkInfo?.run {
            binding.apkPathTv.text = applicationInfo.publicSourceDir
            val apkSignInfo = ApkUtils.getApkSignInfo(context, applicationInfo.packageName)
            binding.apkSha1Tv.text = apkSignInfo.sha1
            binding.apkMd5Tv.text = apkSignInfo.md5
            getSharedPreferencesFiles(applicationInfo.packageName)
            getDatabaseFiles(applicationInfo.packageName)
        }
    }

    private fun getDatabaseFiles(packageName: String) {
        doAsync {
            val sqliteFiles = ShellManager.getSqliteFiles(packageName)
            uiThread {
                if (sqliteFiles.isEmpty()) {
                    binding.defaultDbTv.setText(R.string.none)
                    return@uiThread
                }
                binding.databaseContainer.removeAllViews()
                val params =
                    LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                for (sqliteFile in sqliteFiles) {
                    val textView = AppCompatTextView(context)
                    textView.text = sqliteFile.name
                    textView.textSize = 14F
                    textView.setTextColor(resources.getColor(R.color.item_content_text))
                    textView.setPadding(0, UiUtils.dp2px(context, 8F), 0, 0)
                    textView.tag = sqliteFile
                    binding.databaseContainer.addView(textView, params)
                    textView.setOnClickListener {
                        DatabaseEditActivity.start(context, (it.tag as File).absolutePath)
                    }
                }
            }

        }
    }

    private fun getSharedPreferencesFiles(packageName: String) {
        doAsync {
            val files = AppInfoManager.getSharedPreferencesFiles(packageName)
            uiThread {
                if (files.isEmpty()) {
                    binding.defaultSpTv.setText(R.string.none)
                    return@uiThread
                }
                binding.sharedPreferenceContainer.removeAllViews()
                val params =
                    LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                files.forEach {
                    val textView = AppCompatTextView(context)
                    textView.text = it.name
                    textView.textSize = 14F
                    textView.setTextColor(resources.getColor(R.color.item_content_text))
                    textView.setPadding(0, UiUtils.dp2px(context, 8F), 0, 0)
                    textView.tag = it
                    binding.sharedPreferenceContainer.addView(textView, params)
                    textView.setOnClickListener {
                        SharedPreferenceEditActivity.start(
                            context,
                            (it.tag as File).absolutePath,
                            packageName,
                            apkInfo?.getAppName() ?: ""
                        )
                    }
                }
            }
        }
    }
}