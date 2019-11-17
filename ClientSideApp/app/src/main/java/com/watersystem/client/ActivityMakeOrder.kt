package com.watersystem.client

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.util.Log
import kotlinx.android.synthetic.main.activity_make_order.*
import java.util.ArrayList
import kotlin.concurrent.thread

class ActivityMakeOrder : AppCompatActivity() {

    private var isRunning = true

    private lateinit var mTabLayout: TabLayout
    private lateinit var mPager: ViewPager
    private lateinit var mAdapter: MyPagerAdapter

    private var orderList = IntArray(5){0}//positions => 1=250ml, 2=500ml, 3=1Lit, 4=5Lit, 5=19Lit
    private var currentTab = 0
    private var previousTab = -1

    override fun onDestroy() {
        isRunning = false
        super.onDestroy()
    }

    override  fun onPause() {
        isRunning = false
        super.onPause()
    }

    override fun onResume() {
        isRunning = true
        super.onResume()
    }

    override fun onRestart() {
        isRunning = true
        super.onRestart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_order)
        Log.i("currenttabvalue",currentTab.toString()+"   aaaaaaaa")


        mAdapter = MyPagerAdapter(supportFragmentManager)
        mPager = findViewById(R.id.pager)

        setupViewPager(mPager)
        mTabLayout = findViewById(R.id.tab_layout)
        mTabLayout.setupWithViewPager(mPager)

        btn_add.setOnClickListener {
            val selectedTab = mTabLayout.selectedTabPosition
            orderList[selectedTab]++
            tv_orderQuantity.text = orderList[currentTab].toString()
        }

        btn_remove.setOnClickListener {
            val selectedTab = mTabLayout.selectedTabPosition
            Log.i("currenttabvalue",currentTab.toString()+"   aaaaaaaa")
            if(orderList[selectedTab]!=0) {
                orderList[selectedTab]--
            }
            tv_orderQuantity.text = orderList[currentTab].toString()

        }

        btn_makeOrder.setOnClickListener{
            val intent = Intent(this, ActivityDetectDriver::class.java)

            intent.putExtra("bottle1Capacity", orderList[0])
            intent.putExtra("bottle2Capacity", orderList[1])
            intent.putExtra("bottle3Capacity", orderList[2])
            intent.putExtra("bottle4Capacity", orderList[3])
            intent.putExtra("bottle5Capacity", orderList[4])

            startActivity(intent)
        }

        //this thread keeps track for current selected tab and update the order value accordingly
        thread{
            Log.i("currenttabvalue",currentTab.toString()+"   aaaaaaaa")

            while(true){
                if(!isRunning)continue
                currentTab = mTabLayout.selectedTabPosition
//                Log.i("currenttabvalue",currentTab.toString())
                if(currentTab!=previousTab){//means tab is changed
                    runOnUiThread{
                        tv_orderQuantity.text = orderList[currentTab].toString()
                    }
                    previousTab = currentTab//update the previous tab to detect the next change
                }

            }
        }

    }

//    @SuppressLint("StaticFieldLeak")
//    inner class RunAsync: AsyncTask<Void, Void, ViewPager>(){
//        override fun doInBackground(vararg p0: Void?): ViewPager {
//
//            return mPager
//        }
//
//    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = MyPagerAdapter(supportFragmentManager)

        adapter.addFragment(fragmentTabView().newInstance("250ml")," 250ml ")
        adapter.addFragment(fragmentTabView().newInstance("500ml")," 500ml ")
        adapter.addFragment(fragmentTabView().newInstance("1lit")," 1 Liter ")
        adapter.addFragment(fragmentTabView().newInstance("5lit")," 5 Liter ")
        adapter.addFragment(fragmentTabView().newInstance("19lit")," 19 Liter ")

        viewPager.adapter = adapter
    }
}


class MyPagerAdapter(fm: FragmentManager): FragmentStatePagerAdapter(fm) {

    private val mFragmentList       = ArrayList<Fragment>()
    private val mFragmentTitleList  = ArrayList<String>()

    override fun getItem(position: Int): Fragment {
        return mFragmentList[position]
    }

    override fun getCount(): Int {
        return mFragmentList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mFragmentTitleList[position]
    }

    fun addFragment(fragment: Fragment, title: String){
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }
}

