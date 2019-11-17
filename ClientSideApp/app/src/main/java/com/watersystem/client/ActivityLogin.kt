package com.watersystem.client

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*


class ActivityLogin : AppCompatActivity() {

    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_login.setOnClickListener{
            try {
                login()
            }catch(e: Throwable){

            }
        }

    }

    private fun login(){
        val username = edittext_loginUsername.text.toString()
        val password = edittext_loginPassword.text.toString()

        if(!username.isEmpty() && !password.isEmpty()){
            this.mAuth.signInWithEmailAndPassword("$username@wms.com", password).addOnCompleteListener ( this, { task ->
                if (task.isSuccessful) {
                    showToast("Login Successful")
                    MyPreferences(this).setUserName(username)
                    startActivity(Intent(this@ActivityLogin, ActivityMenu::class.java))
                    finish()
                } else {
                    showToast("Unable to Login")
                }
            })
        }else{
            showToast("Please fill in all the fields")
        }
    }

    private fun showToast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
