package com.wrbug.developerhelper.ui.activity.hierachy

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.base.versionCodeLong
import com.wrbug.developerhelper.commonutil.entity.ApkInfo
import com.wrbug.developerhelper.commonutil.shell.ShellManager
import com.wrbug.developerhelper.ui.decoration.SpaceItemDecoration
import com.wrbug.developerhelper.ui.widget.appdatainfoview.AppDataInfoView
import com.wrbug.developerhelper.ui.widget.appsettingview.AppSettingView
import com.wrbug.developerhelper.ui.widget.layoutinfoview.infopage.InfoAdapter
import com.wrbug.developerhelper.ui.widget.layoutinfoview.infopage.ItemInfo
import com.wrbug.developerhelper.commonutil.UiUtils
import com.wrbug.developerhelper.util.format
import com.wrbug.developerhelper.util.getString
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.util.ArrayList

@Keep
class AppInfoPagerAdapter(
    private val dialog: AppInfoDialog, private val disposable: CompositeDisposable
) : PagerAdapter() {

    private val context: Context = dialog.requireContext()
    private val tabList = arrayListOf<String>()
    private val viewList = arrayListOf<View>()
    private val adapter by lazy {
        InfoAdapter(context, analyzeItem)
    }
    var listener: AppInfoDialogEventListener? = null
    private val itemInfos = ArrayList<Any>()
    private val analyzeItem by lazy {
        ItemInfo(getString(R.string.page_analyze), getString(R.string.click_to_analyze)).apply {
            showCopy = false
            textColor = context.resources.getColor(R.color.colorPrimaryDark)
            setOnClickListener(View.OnClickListener {
                listener?.showHierachyView()
                dialog.dismissAllowingStateLoss()
            })
        }
    }

    fun loadData(apkInfo: ApkInfo?) {
        initAppInfoTab(apkInfo)
        initAppDataInfoTab(apkInfo)
        initAppSettingTab(apkInfo)
        notifyDataSetChanged()
    }

    private fun initAppSettingTab(apkInfo: ApkInfo?) {
        tabList.add(context.getString(R.string.app_setting))
        val view = AppSettingView(context)
        view.setApkInfo(apkInfo)
        viewList.add(view)
    }

    private fun initAppDataInfoTab(apkInfo: ApkInfo?) {
        tabList.add(context.getString(R.string.data_info))
        val appDataInfoView = AppDataInfoView(context)
        viewList.add(appDataInfoView)
        appDataInfoView.apkInfo = apkInfo
    }

    private fun initAppInfoTab(apkInfo: ApkInfo?) {
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
            itemInfos.add(ItemInfo("PackageName", it.packageInfo.packageName))
            it.applicationInfo.className?.let { name ->
                itemInfos.add(ItemInfo("Application", name))
            }
            it.topActivity.takeIf { it.isNotEmpty() }?.let {
                itemInfos.add(ItemInfo("Activity", it))
            }
            itemInfos.add(ItemInfo("VersionName", it.packageInfo.versionName))
            itemInfos.add(ItemInfo("VersionCode", it.packageInfo.versionCodeLong))
            itemInfos.add(ItemInfo("uid", it.applicationInfo.uid))
            ShellManager.getPid(it.packageInfo.packageName).takeUnless { it.isEmpty() }?.let {
                itemInfos.add(ItemInfo("Pid", it))
            }
            itemInfos.add(
                ItemInfo(
                    getString(R.string.first_install_time), it.packageInfo.firstInstallTime.format()
                )
            )
            itemInfos.add(
                ItemInfo(
                    getString(R.string.last_update_time), it.packageInfo.lastUpdateTime.format()
                )
            )
            itemInfos.add(ItemInfo("DataDir", it.applicationInfo.dataDir))
            adapter.setItems(itemInfos)
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