package com.wrbug.developerhelper.xposed.dumpdex

import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap

import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * PackerInfo
 *
 * @author WrBug
 * @since 2018/3/29
 *
 *
 * 加壳类型
 */
object PackerInfo {

    private val sPackageName = ArrayList<String>()
    private val sTypeMap = HashMap<String, Type>()


    /**
     * 加固应用包含的包名，如果无法脱壳，请将application的包名，加到相应数组
     */

    /**
     * 360加固
     */
    private val QI_HOO = arrayOf("com.stub.StubApp")
    /**
     * 爱加密
     */
    private val AI_JIA_MI = arrayOf("s.h.e.l.l.S")
    /**
     * 梆梆加固
     */
    private val BANG_BANG = arrayOf("com.secneo.apkwrapper.ApplicationWrapper")
    /**
     * 腾讯加固
     */
    private val TENCENT = arrayOf("com.tencent.StubShell.TxAppEntry")
    /**
     * 百度加固
     */
    private val BAI_DU = arrayOf("com.baidu.protect.StubApplication")


    init {
        sPackageName.addAll(Arrays.asList(*QI_HOO))
        sPackageName.addAll(Arrays.asList(*AI_JIA_MI))
        sPackageName.addAll(Arrays.asList(*BANG_BANG))
        sPackageName.addAll(Arrays.asList(*TENCENT))
        sPackageName.addAll(Arrays.asList(*BAI_DU))

        for (s in QI_HOO) {
            sTypeMap[s] = Type.QI_HOO
        }
        for (s in AI_JIA_MI) {
            sTypeMap[s] = Type.AI_JIA_MI
        }
        for (s in BANG_BANG) {
            sTypeMap[s] = Type.BANG_BANG
        }
        for (s in TENCENT) {
            sTypeMap[s] = Type.TENCENT
        }
        for (s in BAI_DU) {
            sTypeMap[s] = Type.BAI_DU
        }

    }

    fun log(txt: String) {
        XposedBridge.log("dumpdex.PackerInfo-> $txt")
    }

    fun find(lpparam: XC_LoadPackage.LoadPackageParam): Type? {
        for (s in sPackageName) {
            val clazz = XposedHelpers.findClassIfExists(s, lpparam.classLoader)
            if (clazz != null) {
                log("find class:$s")
                val type = getType(s)
                log("find packerType :" + type!!.name)
                return type
            }
        }
        return null
    }


    private fun getType(packageName: String): Type? {
        return sTypeMap[packageName]
    }

    enum class Type private constructor(enforceName: String) {

        QI_HOO("360加固"),
        AI_JIA_MI("爱加密"),
        BANG_BANG("梆梆加固"),
        TENCENT("腾讯加固"),
        BAI_DU("百度加固");
    }

}
