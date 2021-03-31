package com.zjw.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //lifecycle.addObserver(cosView)

        cosView.setListener(object : RemoteControl.RemoteViewListener{
            override fun clickLeft() {
                Toast.makeText(this@MainActivity, "我点击左边", Toast.LENGTH_SHORT).show()
            }

            override fun clickTop() {
                Toast.makeText(this@MainActivity, "我点击上边", Toast.LENGTH_SHORT).show()

            }

            override fun clickRight() {
                Toast.makeText(this@MainActivity, "我点击右边", Toast.LENGTH_SHORT).show()

            }

            override fun clickBottom() {
                Toast.makeText(this@MainActivity, "我点击下边", Toast.LENGTH_SHORT).show()

            }

            override fun clickCenter() {
                Toast.makeText(this@MainActivity, "我点击中间", Toast.LENGTH_SHORT).show()

            }
        })
    }
}