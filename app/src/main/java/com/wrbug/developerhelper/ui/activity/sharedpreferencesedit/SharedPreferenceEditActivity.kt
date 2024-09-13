package com.wrbug.developerhelper.ui.activity.sharedpreferencesedit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.base.BaseActivity
import com.wrbug.developerhelper.base.setupActionBar
import com.wrbug.developerhelper.commonutil.AppManagerUtils
import com.wrbug.developerhelper.commonutil.addTo
import com.wrbug.developerhelper.commonutil.dpInt
import com.wrbug.developerhelper.commonutil.runOnIO
import com.wrbug.developerhelper.util.startPageLoading
import com.wrbug.developerhelper.util.stopPageLoading
import com.wrbug.developerhelper.databinding.ActivitySharedPreferenceEditBinding
import com.wrbug.developerhelper.ui.decoration.SpaceItemDecoration
import com.wrbug.developerhelper.util.OutSharedPreference
import java.io.File

class SharedPreferenceEditActivity : BaseActivity() {

    private val filePath by lazy {
        intent?.getStringExtra(KEY_FILE_PATH).orEmpty()
    }
    private val filePackageName by lazy {
        intent?.getStringExtra(KEY_PACKAGE_NAME).orEmpty()
    }
    private val appName by lazy {
        intent?.getStringExtra(KEY_APP_NAME).orEmpty()
    }
    private lateinit var adapter: SharedPreferenceListAdapter
    private var saveMenuItem: MenuItem? = null
    private lateinit var binding: ActivitySharedPreferenceEditBinding
    private lateinit var outSharedPreference: OutSharedPreference

    companion object {

        private const val KEY_FILE_PATH = "filePath"
        private const val KEY_PACKAGE_NAME = "packageName"
        private const val KEY_APP_NAME = "appName"
        fun start(context: Context, filePath: String, packageName: String, appName: String) {
            val intent = Intent(context, SharedPreferenceEditActivity::class.java)
            intent.putExtra(KEY_FILE_PATH, filePath)
            intent.putExtra(KEY_PACKAGE_NAME, packageName)
            intent.putExtra(KEY_APP_NAME, appName)
            context.startActivity(intent)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySharedPreferenceEditBinding.inflate(layoutInflater).inject()
        setupActionBar(R.id.toolbar) {
            if (filePath.isNotEmpty()) {
                title = File(filePath).name
            }
        }
        outSharedPreference = OutSharedPreference(this, filePath)
        binding.sprefRv.layoutManager = LinearLayoutManager(this)
        adapter = SharedPreferenceListAdapter(this)
        binding.sprefRv.adapter = adapter
        adapter.setOnValueChangedListener {
            saveMenuItem?.isVisible = it
        }
        val spaceItemDecoration = SpaceItemDecoration(
            24.dpInt(context),
            16.dpInt(context),
            24.dpInt(context),
            0,
            16.dpInt(context),
            40.dpInt(context)
        )
        binding.sprefRv.addItemDecoration(spaceItemDecoration)
        parseXml()
    }

    private fun parseXml() {
        binding.flLoading.startPageLoading()
        outSharedPreference.parse().runOnIO().subscribe({
            binding.emptyView.isVisible = it.isEmpty()
            binding.sprefRv.isVisible = it.isNotEmpty()
            saveMenuItem?.isVisible = false
            adapter.setData(it)
            binding.flLoading.stopPageLoading()
        }, {
            binding.flLoading.stopPageLoading()
        }).addTo(disposable)
    }

    override fun onBackPressed() {
        saveMenuItem?.run {
            if (isVisible) {
                showSaveDialog()
                return
            }
        }
        super.onBackPressed()
    }

    private fun showSaveDialog() {
        AlertDialog.Builder(this).setMessage(getString(R.string.confirm_shared_preference_save))
            .setTitle(R.string.notice).setPositiveButton(R.string.save_and_exit) { _, _ ->
                doSave()
            }.setNegativeButton(getString(R.string.do_not_save)) { _, _ ->
                finish()
            }.show()
    }

    private fun doSave() {
        val data = adapter.getData()
        outSharedPreference.saveToFile(this, data).runOnIO().subscribe({
            if (it) {
                parseXml()
                showDialog(R.string.notice,
                    getString(R.string.save_shared_preference_success, appName),
                    R.string.ok,
                    R.string.cancel,
                    {
                        AppManagerUtils.restartApp(
                            this@SharedPreferenceEditActivity, filePackageName
                        )
                        finish()
                    })
            } else {
                showSnack(getString(R.string.save_shared_preference_failed))
            }
        }, {

        }).addTo(disposable)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save_menu -> {
                doSave()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        outSharedPreference.deleteTmpFile()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_shared_preference_edit, menu)
        saveMenuItem = menu?.findItem(R.id.save_menu)
        return super.onCreateOptionsMenu(menu)
    }
}
