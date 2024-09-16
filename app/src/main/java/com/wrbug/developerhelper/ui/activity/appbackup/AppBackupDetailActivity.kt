package com.wrbug.developerhelper.ui.activity.appbackup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.IntentCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.base.BaseActivity
import com.wrbug.developerhelper.base.ExtraKey
import com.wrbug.developerhelper.databinding.ActivityAppBackupDetailBinding
import com.wrbug.developerhelper.model.entity.BackupAppData
import com.wrbug.developerhelper.ui.adapter.ExMultiTypeAdapter
import com.wrbug.developerhelper.ui.decoration.SpaceItemDecoration
import com.wrbug.developerhelper.util.loadImage

class AppBackupDetailActivity : BaseActivity() {

    companion object {
        fun start(context: Context, info: BackupAppData) {
            context.startActivity(Intent(context, AppBackupDetailActivity::class.java).apply {
                putExtra(ExtraKey.DATA, info)
            })
        }
    }


    private val adapter by ExMultiTypeAdapter.get()

    private val info by lazy {
        IntentCompat.getSerializableExtra(intent, ExtraKey.DATA, BackupAppData::class.java)
    }
    private val binding by lazy {
        ActivityAppBackupDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.appBar.setSubTitle(info?.appName)
        binding.rvAppBackupList.layoutManager = LinearLayoutManager(this)
        binding.rvAppBackupList.adapter = adapter
        binding.rvAppBackupList.addItemDecoration(SpaceItemDecoration.standard)
    }
}