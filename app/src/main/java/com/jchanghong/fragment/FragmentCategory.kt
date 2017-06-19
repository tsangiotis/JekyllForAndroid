package com.jchanghong.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import com.jchanghong.ActivityCategoryDetails
import com.jchanghong.GlobalApplication
import com.jchanghong.R
import com.jchanghong.adapter.ListAdapterCategory
import com.jchanghong.data.DatabaseManager

class FragmentCategory : Fragment() {

  lateinit  private var recyclerView: RecyclerView
   lateinit private var mAdapter: ListAdapterCategory
   lateinit  private var mview: View
   lateinit private var lyt_not_found: LinearLayout
    private val db: DatabaseManager=GlobalApplication.db

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mview = inflater.inflate(R.layout.fragment_category, null)

        //connect db

        recyclerView = mview.findViewById<View>(R.id.recyclerView) as RecyclerView
        lyt_not_found = mview!!.findViewById<View>(R.id.lyt_not_found) as LinearLayout

        recyclerView!!.layoutManager = LinearLayoutManager(activity)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.itemAnimator = DefaultItemAnimator()

        // specify an adapter (see also next example)
        mAdapter = ListAdapterCategory(activity, db!!.allCategory)
        recyclerView!!.adapter = mAdapter
        mAdapter!!.setOnItemClickListener { view, obj, position ->
            val i = Intent(activity, ActivityCategoryDetails::class.java)
            i.putExtra(ActivityCategoryDetails.EXTRA_OBJCT, obj)
            startActivity(i)
        }

        if (mAdapter!!.itemCount == 0) {
            lyt_not_found!!.visibility = View.VISIBLE
        } else {
            lyt_not_found!!.visibility = View.GONE
        }

        return mview
    }

    override fun onResume() {
        super.onResume()
        mAdapter = ListAdapterCategory(activity, db!!.allCategory)
        recyclerView!!.adapter = mAdapter
        mAdapter!!.setOnItemClickListener { view, obj, position ->
            val i = Intent(activity, ActivityCategoryDetails::class.java)
            i.putExtra(ActivityCategoryDetails.EXTRA_OBJCT, obj)
            startActivity(i)
        }
    }
}
