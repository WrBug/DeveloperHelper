package com.wrbug.developerhelper.ui.activity.databaseedit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.evrencoskun.tableview.listener.ITableViewListener
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.base.*
import com.wrbug.developerhelper.model.entity.DatabaseTableInfo
import com.wrbug.developerhelper.commonutil.shell.ShellManager
import com.wrbug.developerhelper.util.DatabaseUtils
import com.wrbug.developerhelper.commonutil.dp2px
import com.wrbug.developerhelper.databinding.ActivityDatabaseEditBinding
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class DatabaseEditActivity: BaseActivity() {

    private var filePath: String? = ""
    private lateinit var dbPath: File
    private lateinit var dataBinding: ActivityDatabaseEditBinding
    private val tableNames = ArrayList<String>()
    private var dbMap: Map<String, DatabaseTableInfo> = TreeMap()
    private val adapter = DatabaseTableAdapter(this)
    private var selectedIndex = 0
    private val dstDir: File by lazy {
        val file = File(externalCacheDir, "db")
        if (file.exists()) {
            file.mkdir()
        }
        file
    }

    companion object {

        fun start(context: Context, filePath: String) {
            val intent = Intent(context, DatabaseEditActivity::class.java)
            intent.putExtra("filePath", filePath)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = ActivityDatabaseEditBinding.inflate(layoutInflater)
        setContentView(dataBinding.root)
        intent?.run {
            filePath = getStringExtra("filePath")
        }
        if (filePath.isNullOrEmpty()) {
            showToast(getString(R.string.get_database_failed))
            finish()
            return
        }
        dbPath = File(filePath)
        setupActionBar(R.id.toolbar) {
            title = dbPath.name
        }
        initTableView()
        readDatabase()
    }

    private fun initTableView() {
        dataBinding.tableView.adapter = adapter
        dataBinding.tableView.tableViewListener = object: ITableViewListener {
            override fun onCellLongPressed(p0: RecyclerView.ViewHolder, p1: Int, p2: Int) {

            }

            override fun onColumnHeaderLongPressed(p0: RecyclerView.ViewHolder, p1: Int) {
            }

            override fun onRowHeaderClicked(p0: RecyclerView.ViewHolder, p1: Int) {
            }

            override fun onColumnHeaderClicked(p0: RecyclerView.ViewHolder, p1: Int) {
            }

            override fun onCellClicked(p0: RecyclerView.ViewHolder, p1: Int, p2: Int) {
            }

            override fun onRowHeaderLongPressed(p0: RecyclerView.ViewHolder, p1: Int) {
            }

        }
    }

    private fun readDatabase() {
        doAsync {
            if (dstDir.exists().not()) {
                dstDir.mkdir()
            }
            val success =
                ShellManager.cpFile(dbPath.absolutePath, "${dstDir.absolutePath}/${dbPath.name}")
            if (!success) {
                uiThread {
                    showToast(getString(R.string.get_database_failed))
                    finish()
                }
                return@doAsync
            }
            val file = File(dstDir, dbPath.name)
            dbMap = DatabaseUtils.getDatabase(file.absolutePath)
            file.delete()
            tableNames.addAll(dbMap.keys)
            uiThread {
                setTableContainer()
            }
            selectTable(0)
        }
    }

    private fun setTableContainer() {
        dataBinding.tableNameContainer.removeAllViews()
        val layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        val dp10 = dp2px(10F)
        val dp20 = dp2px(20F)
        tableNames.forEachIndexed { index, name ->
            val tv = TextView(this)
            tv.text = name
            tv.tag = index
            if (index == 0) {
                tv.setTextColor(resources.getColor(R.color.colorPrimary))
            } else {
                tv.setTextColor(resources.getColor(R.color.text_color_666666))
            }
            tv.setPadding(dp10, dp10, dp10, dp20)
            tv.setOnClickListener {
                selectTable(it.tag as Int)
            }
            dataBinding.tableNameContainer.addView(tv, layoutParams)
        }
    }

    private fun selectTable(position: Int) {
        doAsync {
            if (tableNames.size <= position) {
                return@doAsync
            }
            val tableName = tableNames[position]
            val databaseTableInfo = dbMap[tableName]
            databaseTableInfo?.run {
                val keyList = arrayListOf(*keys)
                val rowHeaders = ArrayList<Int>()
                val cell = ArrayList<MutableList<String?>>()
                rows.forEach {
                    val list = ArrayList<String?>()
                    rowHeaders.add(rowHeaders.size + 1)
                    keys.forEach { key ->
                        list.add(it[key])
                    }
                    cell.add(list)
                }
                uiThread {
                    setHasData(!cell.isEmpty())
                    adapter.setAllItems(keyList, rowHeaders, cell)
                    var tv = dataBinding.tableNameContainer.getChildAt(selectedIndex) as TextView
                    tv.setTextColor(resources.getColor(R.color.text_color_666666))
                    tv = dataBinding.tableNameContainer.getChildAt(position) as TextView
                    tv.setTextColor(resources.getColor(R.color.colorPrimary))
                    selectedIndex = position
                }
            }
        }
    }

    private fun setHasData(hasData: Boolean) {
        dataBinding.noDataTv.visibility = if (hasData) View.GONE else View.VISIBLE
        dataBinding.tableView.visibility = if (hasData) View.VISIBLE else View.GONE
    }
}
