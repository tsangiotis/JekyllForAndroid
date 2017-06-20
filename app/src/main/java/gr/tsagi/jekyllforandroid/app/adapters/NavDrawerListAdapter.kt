package gr.tsagi.jekyllforandroid.app.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

import java.util.ArrayList

import gr.tsagi.jekyllforandroid.app.R
import gr.tsagi.jekyllforandroid.app.utils.NavDrawerItem

/**
 * Created by tsagi on 7/9/14.
 */

class NavDrawerListAdapter(private val context: Context, private val navDrawerItems: ArrayList<NavDrawerItem>) : BaseAdapter() {

    override fun getCount(): Int {
        return navDrawerItems.size
    }

    override fun getItem(position: Int): Any {
        return navDrawerItems[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val mInflater = context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = mInflater.inflate(R.layout.drawer_list_item, null)
        }

        val imgIcon = convertView!!.findViewById<View>(R.id.icon) as ImageView
        val txtTitle = convertView.findViewById<View>(R.id.title) as TextView

        imgIcon.setImageResource(navDrawerItems[position].icon)
        txtTitle.text = navDrawerItems[position].title

        // displaying count
        // check whether it set visible or not

        return convertView
    }

}
