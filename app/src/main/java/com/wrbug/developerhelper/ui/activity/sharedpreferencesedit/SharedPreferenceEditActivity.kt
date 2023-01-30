package com.wrbug.developerhelper.ui.activity.sharedpreferencesedit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.basecommon.BaseActivity
import com.wrbug.developerhelper.basecommon.setupActionBar
import com.wrbug.developerhelper.commonutil.AppManagerUtils
import com.wrbug.developerhelper.commonutil.shell.ShellManager
import com.wrbug.developerhelper.ui.decoration.SpaceItemDecoration
import com.wrbug.developerhelper.util.OutSharedPreferenceManager
import com.wrbug.developerhelper.util.XmlUtil
import com.wrbug.developerhelper.commonutil.dp2px
import com.wrbug.developerhelper.databinding.ActivitySharedPreferenceEditBinding
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File

class SharedPreferenceEditActivity: BaseActivity(),
                                    SharedPreferenceListAdapter.OnValueChangedListener {

    private var filePath: String = ""
    private var filePackageName = ""
    private var appName = ""
    private lateinit var adapter: SharedPreferenceListAdapter
    private var saveMenuItem: MenuItem? = null
    private lateinit var binding: ActivitySharedPreferenceEditBinding

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
        intent?.let {
            filePath = it.getStringExtra(KEY_FILE_PATH).orEmpty()
            filePackageName = it.getStringExtra(KEY_PACKAGE_NAME).orEmpty()
            appName = it.getStringExtra(KEY_APP_NAME).orEmpty()
        }
        setupActionBar(R.id.toolbar) {
            if (filePath.isNotEmpty()) {
                title = File(filePath).name
            }
        }

        binding.sprefRv.layoutManager = LinearLayoutManager(this)
        adapter = SharedPreferenceListAdapter(this)
        binding.sprefRv.adapter = adapter
        adapter.setOnValueChangedListener(this)
        val spaceItemDecoration = SpaceItemDecoration(dp2px(10F))
        binding.sprefRv.addItemDecoration(spaceItemDecoration)
        parseXml()

    }

    override fun onChanged(changed: Boolean) {
        saveMenuItem?.isVisible = changed
    }

    private fun parseXml() {
        doAsync {
            val xml = ShellManager.catFile(filePath)
            val list = XmlUtil.parseSharedPreference(xml)
            uiThread {
                saveMenuItem?.isVisible = false
                adapter.setData(list)
            }
        }
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
            .setTitle(R.string.notice)
            .setPositiveButton(R.string.save_and_exit) { _, _ ->
                if (doSave()) {
                    showDialog(
                        R.string.notice,
                        getString(R.string.save_shared_preference_success, appName),
                        R.string.ok,
                        R.string.cancel,
                        {
                            AppManagerUtils.restartApp(
                                this@SharedPreferenceEditActivity,
                                filePackageName
                            )
                            finish()
                        }, {
                            finish()
                        }
                    )
                } else {
                    showSnack(getString(R.string.save_shared_preference_failed))
                }
            }
            .setNegativeButton(getString(R.string.do_not_save)) { _, _ ->
                finish()
            }.show()
    }

    private fun doSave(): Boolean {
        val data = adapter.getData()
        val file = OutSharedPreferenceManager.saveToFile(this, data)
        val success = ShellManager.catFile(file.absolutePath, filePath, "666")
        file.delete()
        return success
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save_menu -> {
                if (!doSave()) {
                    showSnack(getString(R.string.save_shared_preference_failed))
                    return super.onOptionsItemSelected(item)
                }
                parseXml()
                showDialog(
                    R.string.notice,
                    getString(R.string.save_shared_preference_success, appName),
                    R.string.ok,
                    R.string.cancel,
                    {
                        AppManagerUtils.restartApp(
                            this@SharedPreferenceEditActivity,
                            filePackageName
                        )
                        finish()
                    }
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_shared_preference_edit, menu)
        saveMenuItem = menu?.findItem(R.id.save_menu)
        return super.onCreateOptionsMenu(menu)
    }
}
