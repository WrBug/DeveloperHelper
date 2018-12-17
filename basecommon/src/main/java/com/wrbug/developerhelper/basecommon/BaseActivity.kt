package com.wrbug.developerhelper.basecommon

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

abstract class BaseActivity : AppCompatActivity() {
    lateinit var toastRootView: View
    protected lateinit var context: BaseActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        context = this
        super.onCreate(savedInstanceState)
    }

    override fun setContentView(layoutResID: Int) {
        toastRootView = layoutInflater.inflate(layoutResID, null)
        setContentView(toastRootView)
    }

    fun showSnack(msg: String) {
        Snackbar.make(toastRootView, msg, Snackbar.LENGTH_SHORT).show()
    }

    fun showSnack(id: Int) {
        Snackbar.make(toastRootView, id, Snackbar.LENGTH_SHORT).show()
    }
}