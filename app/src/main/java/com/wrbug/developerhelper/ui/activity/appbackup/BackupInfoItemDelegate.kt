package com.wrbug.developerhelper.ui.activity.appbackup

import android.view.LayoutInflater
import android.view.ViewGroup
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.commonutil.SpannableBuilder
import com.wrbug.developerhelper.databinding.ItemBackupAppInfoBinding
import com.wrbug.developerhelper.model.entity.BackupAppData
import com.wrbug.developerhelper.ui.adapter.delegate.BaseItemViewBindingDelegate
import com.wrbug.developerhelper.util.format
import com.wrbug.developerhelper.util.getString
import com.wrbug.developerhelper.util.loadImage
import com.wrbug.developerhelper.util.setOnDoubleCheckClickListener

class BackupInfoItemDelegate(private val listener: (BackupAppData) -> Unit) :
    BaseItemViewBindingDelegate<BackupAppData, ItemBackupAppInfoBinding>() {
    override fun onBindViewHolder(binding: ItemBackupAppInfoBinding, item: BackupAppData) {
        binding.tvAppName.text = item.appName
        binding.tvAppPackageName.text = item.packageName
        val size = item.backupMap.size
        binding.tvBackupCount.text = SpannableBuilder.with(
            binding.root.context,
            R.string.app_info_backup_count.getString(size)
        ).addSpanWithBold(size.toString()).build()
        val time = item.backupMap.values.maxByOrNull { it.time }?.time ?: 0
        binding.tvLastBackupTime.text = R.string.last_backup_time.getString(time.format())
        binding.ivIcon.loadImage(item.iconPath, R.drawable.ic_default_app_ico_place_holder)
        binding.root.setOnDoubleCheckClickListener {
            listener(item)
        }
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemBackupAppInfoBinding {
        return ItemBackupAppInfoBinding.inflate(inflater, parent, false)
    }
}