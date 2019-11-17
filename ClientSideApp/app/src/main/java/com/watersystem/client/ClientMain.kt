package com.watersystem.client

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_client_main.*

class ClientMain : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_main)

        if(MyPreferences(this).getUserName()!=""){
            startActivity(Intent(this, ActivityMenu::class.java))
            finish()
        }else {
            btn_login.setOnClickListener {
                startActivity(Intent(this, ActivityLogin::class.java))
            }

            btn_register.setOnClickListener {
                startActivity(Intent(this, ActivityRegister::class.java))
            }
        }

    }
}
