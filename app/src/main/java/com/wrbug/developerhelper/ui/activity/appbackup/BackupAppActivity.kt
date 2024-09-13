package com.wrbug.developerhelper.ui.activity.appbackup

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.drakeet.multitype.MultiTypeAdapter
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.base.BaseActivity
import com.wrbug.developerhelper.base.setupActionBar
import com.wrbug.developerhelper.commonutil.addTo
import com.wrbug.developerhelper.commonutil.dpInt
import com.wrbug.developerhelper.commonutil.runOnIO
import com.wrbug.developerhelper.databinding.ActivityBackupAppBinding
import com.wrbug.developerhelper.ui.adapter.ExMultiTypeAdapter
import com.wrbug.developerhelper.ui.decoration.SpaceItemDecoration
import com.wrbug.developerhelper.util.BackupUtils

class BackupAppActivity : BaseActivity() {
    private val binding by lazy {
        ActivityBackupAppBinding.inflate(layoutInflater)
    }
    private val adapter by lazy {
        ExMultiTypeAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        loadData()
    }

    private fun initView() {
        binding.rvAppList.layoutManager = LinearLayoutManager(this)
        binding.rvAppList.adapter = adapter
        binding.rvAppList.addItemDecoration(
            SpaceItemDecoration(
                24.dpInt(this),
                12.dpInt(this),
                24.dpInt(this),
                12.dpInt(this),
                24.dpInt(this),
                40.dpInt(this)
            )
        )
        adapter.register(BackupInfoItemDelegate())
    }

    private fun loadData() {
        adapter.showLoading()
        BackupUtils.getAllBackupInfo().runOnIO().subscribe({
            if (it.isEmpty()) {
                adapter.showEmpty()
                return@subscribe
            }
            adapter.loadData(it)
        }, {
            adapter.showEmpty()
        }).addTo(disposable)
    }
}