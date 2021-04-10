package com.zjw.customview

import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        showTestDialog()

        //lifecycle.addObserver(cosView)

        //loadView.startMoving()

    }

    private fun showTestDialog() {
        clickBt.setOnClickListener {
            val dialog= AlertDialog.Builder(this,R.style.custom_dialog)
            dialog.setView(R.layout.dialog_show)
            dialog.create().show()

            val window: Window = window
            val layoutParams: WindowManager.LayoutParams = window.attributes
            layoutParams.gravity=Gravity.BOTTOM
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = layoutParams
            //window.setContentView()
        }
    }
}