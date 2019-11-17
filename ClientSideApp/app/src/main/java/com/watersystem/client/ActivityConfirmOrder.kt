package com.watersystem.client

import android.graphics.Color
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_confirm_order.*
import kotlinx.android.synthetic.main.activity_confirm_order.view.*
import java.net.CacheRequest
import java.sql.Time
import java.util.*

class ActivityConfirmOrder : AppCompatActivity() {

    private lateinit var mDatabase: DatabaseReference
    private lateinit var custRequestReference: DatabaseReference
    private var uid = FirebaseAuth.getInstance().uid
    private val orderID = uid+Calendar.getInstance().timeInMillis
    private var bill = mutableListOf<Bill>()

    private var stationID = ""
    private var finalBill = 0
    var isStationReady = "1"

    private val lock = java.lang.Object()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_order)

        //items that are ordered
        //supplier id
        //pick up location
        //finally place order in customer request from this activity

        val bundle = intent.extras
        stationID = bundle.getString("stationID")

        mDatabase = FirebaseDatabase.getInstance().getReference("wms-database")
        custRequestReference = FirebaseDatabase.getInstance().getReference("wms-database")

        mDatabase.child("STATION").child(stationID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(snap: DataSnapshot?) {
                bill.add(Bill("250ml",
                        bundle.getInt("bottle1Capacity"),
                        snap!!.child("INVENTORY").child("bottle1").child("costPerItem").value.toString().toInt()))


                bill.add(Bill("500ml",
                        bundle.getInt("bottle2Capacity"),
                        snap.child("INVENTORY").child("bottle2").child("costPerItem").value.toString().toInt()))

                bill.add(Bill("1 Liter",
                        bundle.getInt("bottle3Capacity"),
                        snap.child("INVENTORY").child("bottle3").child("costPerItem").value.toString().toInt()))

                bill.add(Bill("5 Liter",
                        bundle.getInt("bottle4Capacity"),
                        snap.child("INVENTORY").child("bottle4").child("costPerItem").value.toString().toInt()))

                bill.add(Bill("19 Liter",
                        bundle.getInt("bottle5Capacity"),
                        snap.child("INVENTORY").child("bottle5").child("costPerItem").value.toString().toInt()))
//
//                productPriceList.add(snap.child("inventory").child("bottle2").child("costPerItem").value.toString().toInt())
//                productPriceList.add(snap.child("inventory").child("bottle3").child("costPerItem").value.toString().toInt())
//                productPriceList.add(snap.child("inventory").child("bottle4").child("costPerItem").value.toString().toInt())
//                productPriceList.add(snap.child("inventory").child("bottle5").child("costPerItem").value.toString().toInt())

                showFinalBill()
            }
        })

        custRequestReference.child("ORDERREQUESTS").child(stationID).runTransaction(object : Transaction.Handler {
            override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {

            }

            override fun doTransaction(mutableData: MutableData?): Transaction.Result {

                isStationReady = mutableData!!.child("isStationReady").value.toString()
                Log.i("isStationReadyyyyy1", mutableData.child("isStationReady").value.toString())
                if(isStationReady=="1"){
                    mutableData.child("isStationReady").value = 0
                    isStationReady = mutableData.child("isStationReady").value.toString()
                    Log.i("isStationReadyyyyy2",isStationReady)

                    //place your order now
                    placeOrder(mutableData)

                    mutableData.child("isStationReady").value = 1
                    isStationReady = mutableData.child("isStationReady").value.toString()
                    Log.i("isStationReadyyyyy3",isStationReady)
                }else if(isStationReady == "0"){ //ignore this condition for now

                    Log.i("isStationReadyyyyy2","enteredddd")


                    while (isStationReady!="1") {
                        Log.i("isStationReadyyyyy2","loopinggg")
                    }
                    Log.i("isStationReadyyyyy2","passsed loop")

                    mutableData.child("isStationReady").value = 0
                    isStationReady = mutableData.child("isStationReady").value.toString()
                    Log.i("isStationReadyyyyy2",isStationReady)

                    //place your order now
                    placeOrder(mutableData)
                    var i=0
                    while(i<100000){
                        i++
                    }

                    mutableData.child("isStationReady").value = 1
                    isStationReady = mutableData.child("isStationReady").value.toString()
                    Log.i("isStationReadyyyyy3",isStationReady)

                }

                return Transaction.success(mutableData)
            }

        })



    }

    private fun placeOrder(mutableData: MutableData){
        for(product in bill) {
            if(product.noOfBottles == 0)continue
            mutableData.child("orders")
                    .child(orderID)
                    .child("orderBottles")
                    .child(product.bottleNo).value = product.noOfBottles
        }
    }

    private fun isStationReadyToServe(): Boolean{
        var isFireBaseExecutionDone = false
        custRequestReference.child("ORDERREQUESTS").child(stationID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                isFireBaseExecutionDone = true
            }

            override fun onDataChange(snap: DataSnapshot?) {
                isStationReady = snap!!.child("isStationReady").value.toString()
                isFireBaseExecutionDone = true
            }
        })

        while(!isFireBaseExecutionDone){//keep looping
            synchronized(lock) {
                lock.wait(1000)
            }
        }
        return isStationReady == "1"
    }

    private fun showFinalBill(){
        val bottleNameTextView = TextView(this)
        val noOfbottlesTextView = TextView(this)
        val bottlePriceTextView = TextView(this)

        bottleNameTextView.text = "Bottle"
        noOfbottlesTextView.text = "Quantity"
        bottlePriceTextView.text = "Per Bottle Cost"

        bottleNameTextView.setTypeface(
                null,
                Typeface.BOLD
        )

        noOfbottlesTextView.setTypeface(
                null,
                Typeface.BOLD
        )

        bottlePriceTextView.setTypeface(
                null,
                Typeface.BOLD
        )

        bottleNameTextView.textSize = 20f
        noOfbottlesTextView.textSize = 20f
        bottlePriceTextView.textSize = 20f

        bottleNameTextView.gravity = Gravity.START
        noOfbottlesTextView.gravity = Gravity.CENTER_HORIZONTAL
        bottlePriceTextView.gravity = Gravity.END

        val row = TableRow(this)
        val lp = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT)
        row.layoutParams = lp
        row.setBackgroundColor(Color.GRAY)

        row.addView(bottleNameTextView)
        row.addView(noOfbottlesTextView)
        row.addView(bottlePriceTextView)

        table_confirmOrder.addView(row)
        var finalIndex=0
        for(product in bill){
            if(product.noOfBottles == 0)continue

            val row1 = TableRow(this)
            val lp1 = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT)
            row1.layoutParams = lp1
//            row1.setBackgroundColor(Color.GRAY)


            val rowBottleNameTextView = TextView(this)
            val rowNoOfbottlesTextView = TextView(this)
            val rowBottlePriceTextView = TextView(this)

            rowBottleNameTextView.textSize = 20f
            rowNoOfbottlesTextView.textSize = 20f
            rowBottlePriceTextView.textSize = 20f

            rowBottleNameTextView.gravity = Gravity.START
            rowNoOfbottlesTextView.gravity = Gravity.CENTER_HORIZONTAL
            rowBottlePriceTextView.gravity = Gravity.END

            rowBottleNameTextView.text = product.bottleNo
            rowNoOfbottlesTextView.text = product.noOfBottles.toString()
            rowBottlePriceTextView.text = product.bottlePrice.toString()

            row1.addView(rowBottleNameTextView)
            row1.addView(rowNoOfbottlesTextView)
            row1.addView(rowBottlePriceTextView)

            table_confirmOrder.addView(row1)
            finalIndex++

            finalBill += product.noOfBottles * product.bottlePrice

        }
        /* CALCULATING TOTAL AND MAKING ITS ROW */
        val totalTextView = TextView(this)
        val totalPriceTextView = TextView(this)

        totalTextView.text = "Total"
        totalPriceTextView.text = finalBill.toString()

        totalTextView.gravity = Gravity.START
        totalPriceTextView.gravity = Gravity.END

        val row2 = TableRow(this)
        val lp2 = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT)
        lp2.topMargin = 30
        row2.layoutParams = lp2
        row2.setBackgroundColor(Color.GRAY)

        totalTextView.textSize = 20f
        totalPriceTextView.textSize = 20f

        totalTextView.setTypeface(
                null,
                Typeface.BOLD
        )

        totalPriceTextView.setTypeface(
                null,
                Typeface.BOLD
        )


        row2.addView(totalTextView)
        row2.addView(totalPriceTextView)

        table_confirmOrder.addView(row2)

//        finalBill = (bottle1Orders*productPriceList[0]) + (bottle1Orders*productPriceList[1]) + (bottle1Orders*productPriceList[2])
//        + (bottle1Orders*productPriceList[3]) + (bottle1Orders*productPriceList[4])

        Toast.makeText(this, finalBill.toString(), Toast.LENGTH_LONG).show()

    }
}
