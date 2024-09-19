package com.wrbug.developerhelper.ui.widget.backupprogress

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.databinding.ViewBackupProgressBinding
import com.wrbug.developerhelper.util.getString

class BackupProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {
    private val binding = ViewBackupProgressBinding.inflate(LayoutInflater.from(context), this)


    fun setTitle(title: Int) {
        binding.tvTitle.text = getString(title)
    }

    fun setStatus(status: Status, successPath: String = "") {
        when (status) {
            Status.Processing -> {
                binding.progress.isVisible = true
                binding.ivStatus.isVisible = false
                binding.tvStatus.text = context.getString(R.string.backuping_data)
            }

            Status.Success -> {
                binding.progress.isVisible = false
                binding.ivStatus.isVisible = true
                binding.ivStatus.setImageResource(R.drawable.ic_success)
                binding.ivStatus.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        context,
                        R.color.material_color_green_400
                    )
                )
                binding.tvStatus.text = successPath
            }

            Status.Failed -> {
                binding.progress.isVisible = false
                binding.ivStatus.isVisible = true
                binding.tvStatus.text = context.getString(R.string.backup_fail)
                binding.ivStatus.setImageResource(R.drawable.ic_fail)
                binding.ivStatus.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        context,
                        R.color.material_color_red_400
                    )
                )

            }

            Status.Ignore -> {
                binding.progress.isVisible = false
                binding.ivStatus.isVisible = true
                binding.tvStatus.text = context.getString(R.string.ignore_backup)
                binding.ivStatus.setImageResource(R.drawable.ic_ignore)
                binding.ivStatus.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        context,
                        R.color.material_color_grey_400
                    )
                )
            }

            Status.Waiting -> {
                binding.progress.isVisible = true
                binding.ivStatus.isVisible = true
                binding.tvStatus.text = context.getString(R.string.backup_waiting)
                binding.ivStatus.setImageResource(R.drawable.ic_waiting)
                binding.ivStatus.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.material_color_grey_400)
                )
            }

            Status.Canceled -> {
                binding.progress.isVisible = false
                binding.ivStatus.isVisible = true
                binding.ivStatus.setImageResource(R.drawable.ic_canceled)
                binding.tvStatus.text = context.getString(R.string.canceled)
                binding.ivStatus.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.material_color_grey_400)
                )
            }
        }
    }


    enum class Status {
        Processing, Success, Failed, Ignore, Waiting, Canceled
    }
}