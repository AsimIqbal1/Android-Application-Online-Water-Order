package com.watersystem.client

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.watersystem.client.R
import kotlinx.android.synthetic.main.activity_menu.*

class ActivityMenu : AppCompatActivity() {

    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        btn_menu_logout.setOnClickListener {
            mAuth.signOut()
            MyPreferences(this).setUserName("")
            startActivity(Intent(this, ClientMain::class.java))
            finish()
        }

        btn_Order.setOnClickListener {
            startActivity(Intent(this, ActivityMakeOrder::class.java))
        }

    }
}
