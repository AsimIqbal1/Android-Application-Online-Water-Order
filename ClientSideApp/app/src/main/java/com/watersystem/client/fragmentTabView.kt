package com.watersystem.client

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.fragment_fragment_tab_view.view.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [fragmentTabView.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [fragmentTabView.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class fragmentTabView : Fragment() {

    private lateinit var mView: View
    private val itemValueString = "itemValue"
    private val itemIDString = "itemID"
    private lateinit var itemImg: ImageView

    fun newInstance(itemID: String, itemPrice: Int = 0): fragmentTabView{
        val myFragment = fragmentTabView()
        val arguments = Bundle()
        arguments.putString(itemIDString,itemID)
        myFragment.arguments = arguments
        return myFragment
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val arguments = arguments
        val itID = arguments!!.getString(itemIDString)
        mView = inflater.inflate(R.layout.fragment_fragment_tab_view, container, false)
        itemImg = mView.itemImageView

        when (itID) {
            "250ml" -> itemImg.setImageResource(R.drawable.ml250mineralwaterbottle)
            "500ml" -> itemImg.setImageResource(R.drawable.ml500waterbottle)
            "1lit" -> itemImg.setImageResource(R.drawable.liter1waterbottle)
            "5lit" -> itemImg.setImageResource(R.drawable.liter5waterbottle)
            "19lit" -> itemImg.setImageResource(R.drawable.liter19waterbottle)
            else -> {
                //do nothing
            }
        }

        return mView
    }

}

