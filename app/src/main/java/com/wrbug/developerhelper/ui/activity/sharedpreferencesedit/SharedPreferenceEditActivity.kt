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
import com.wrbug.developerhelper.shell.ShellManager
import com.wrbug.developerhelper.ui.decoration.SpaceItemDecoration
import com.wrbug.developerhelper.util.OutSharedPreferenceManager
import com.wrbug.developerhelper.util.XmlUtil
import com.wrbug.developerhelper.util.dp2px
import kotlinx.android.synthetic.main.activity_shared_preference_edit.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File

class SharedPreferenceEditActivity : BaseActivity(), SharedPreferenceListAdapter.OnValueChangedListener {


    private var filePath: String = ""
    private lateinit var adapter: SharedPreferenceListAdapter
    private var saveMenuItem: MenuItem? = null

    companion object {
        fun start(context: Context, filePath: String) {
            val intent = Intent(context, SharedPreferenceEditActivity::class.java)
            intent.putExtra("filePath", filePath)
            context.startActivity(intent)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shared_preference_edit)
        intent?.let {
            filePath = it.getStringExtra("filePath")
        }
        setupActionBar(R.id.toolbar) {
            if (filePath.isNotEmpty()) {
                title = File(filePath).name
            }
        }

        sprefRv.layoutManager = LinearLayoutManager(this)
        adapter = SharedPreferenceListAdapter(this)
        sprefRv.adapter = adapter
        adapter.setOnValueChangedListener(this)
        val spaceItemDecoration = SpaceItemDecoration(dp2px(10F))
        sprefRv.addItemDecoration(spaceItemDecoration)
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
                    showSnack(getString(R.string.save_shared_preference_success))
                    finish()
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.run {
            when (itemId) {
                R.id.save_menu -> {
                    if (!doSave()) {
                        showSnack(getString(R.string.save_shared_preference_failed))
                        return@run
                    }
                    showSnack(getString(R.string.save_shared_preference_success))
                    parseXml()
                }
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
