package com.wrbug.developerhelper.ui.activity.hierachy

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.commonutil.entity.ApkInfo
import com.wrbug.developerhelper.commonutil.entity.TopActivityInfo
import com.wrbug.developerhelper.commonutil.shell.ShellManager
import com.wrbug.developerhelper.ui.decoration.SpaceItemDecoration
import com.wrbug.developerhelper.ui.widget.appdatainfoview.AppDataInfoView
import com.wrbug.developerhelper.ui.widget.appsettingview.AppSettingView
import com.wrbug.developerhelper.ui.widget.layoutinfoview.infopage.InfoAdapter
import com.wrbug.developerhelper.ui.widget.layoutinfoview.infopage.ItemInfo
import com.wrbug.developerhelper.util.EnforceUtils
import com.wrbug.developerhelper.commonutil.UiUtils
import com.wrbug.developerhelper.ipc.processshare.manager.AppXposedProcessDataManager
import com.wrbug.developerhelper.util.format
import com.wrbug.developerhelper.util.getString
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.StringBuilder
import java.util.ArrayList

@Keep
class AppInfoPagerAdapter(
    private val dialog: AppInfoDialog,
    private val apkInfo: ApkInfo?,
    private val topActivity: TopActivityInfo?
):
    PagerAdapter() {

    private val context: Context = dialog.requireContext()
    private val tabList = arrayListOf<String>()
    private val viewList = arrayListOf<View>()
    private val adapter = InfoAdapter(context)
    private val enforceItem =
        ItemInfo(context.getString(R.string.enforce_type), context.getString(R.string.analyzing))
    var listener: AppInfoDialogEventListener? = null
    private val itemInfos = ArrayList<ItemInfo>()

    init {
        initAppInfoTab()
        initAppDataInfoTab()
        initAppSettingTab()
    }

    private fun initAppSettingTab() {
        tabList.add(context.getString(R.string.app_setting))
        val view = AppSettingView(context)
        view.apkInfo = apkInfo
        viewList.add(view)
    }

    private fun initAppDataInfoTab() {
        tabList.add(context.getString(R.string.data_info))
        val appDataInfoView = AppDataInfoView(context)
        viewList.add(appDataInfoView)
        appDataInfoView.apkInfo = apkInfo
    }

    private fun initAppInfoTab() {
        tabList.add(context.getString(R.string.base_info))
        val rv = RecyclerView(context)
        viewList.add(rv)
        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = adapter
        val itemDecoration = SpaceItemDecoration(0)
        itemDecoration.setLastBottomPadding(UiUtils.dp2px(context, 10F))
        itemDecoration.setFirstTopPadding(UiUtils.dp2px(context, 10F))
        rv.addItemDecoration(itemDecoration)
        apkInfo?.let { it ->
            val item =
                ItemInfo(getString(R.string.page_analyze), getString(R.string.click_to_analyze))
            item.setOnClickListener(View.OnClickListener {
                listener?.showHierachyView()
                dialog.dismissAllowingStateLoss()
            })
            item.textColor = context.resources.getColor(R.color.colorPrimaryDark)
            itemInfos.add(item)
            headerItemHook(it.packageInfo, it.applicationInfo, itemInfos)
            topActivity?.let {
                itemInfos.add(ItemInfo("PackageName", it.packageName))
                itemInfos.add(ItemInfo("Activity", it.activity))
                it.fragments?.takeUnless { it.isEmpty() }
                    ?.map { it.name + "(state=${it.state},tag=${it.tag})" }
                    ?.joinToString("\n")?.let {
                        itemInfos.add(ItemInfo("Fragments", it))
                    }
            }
            itemInfos.add(ItemInfo("VersionName", it.packageInfo.versionName))
            itemInfos.add(ItemInfo("VersionCode", it.packageInfo.versionCode))
            it.applicationInfo.className?.let { name ->
                itemInfos.add(ItemInfo("Application", name))
            }
            itemInfos.add(enforceItem)
            itemInfos.add(ItemInfo("uid", it.applicationInfo.uid))
            ShellManager.getPid(it.packageInfo.packageName).takeUnless {
                it.isEmpty()
            }?.let {
                itemInfos.add(ItemInfo("Pid", it))
            }
            itemInfos.add(
                ItemInfo(
                    getString(R.string.first_install_time),
                    it.packageInfo.firstInstallTime.format()
                )
            )
            itemInfos.add(
                ItemInfo(
                    getString(R.string.last_update_time),
                    it.packageInfo.lastUpdateTime.format()
                )
            )
            itemInfos.add(ItemInfo("DataDir", it.applicationInfo.dataDir))
            adapter.setItems(itemInfos)
            getEnforce(it.packageInfo.packageName)
        }
    }

    private fun headerItemHook(
        i: PackageInfo,
        info: ApplicationInfo,
        itemInfo: ArrayList<ItemInfo>
    ) {
    }

    fun findItemById(id: String): ItemInfo? {
        itemInfos.forEach {
            if (it.id == id) {
                return it
            }
        }
        return null
    }

    private fun setEnforceType(type: EnforceUtils.EnforceType) {
        enforceItem.content = type.type
    }

    private fun getEnforce(packageName: String) {
        doAsync {
            val type = EnforceUtils.getEnforceType(packageName)
            uiThread {
                setEnforceType(type)
                adapter.notifyItemChanged(enforceItem)
            }

        }
    }

    override fun isViewFromObject(view: View, o: Any): Boolean {
        return view == o
    }

    override fun getCount(): Int {
        return tabList.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(viewList[position])
        return viewList[position]

    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabList[position]
    }
}