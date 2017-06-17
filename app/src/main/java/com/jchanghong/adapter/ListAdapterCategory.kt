package com.jchanghong.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.jchanghong.R
import com.jchanghong.model.Category
import com.jchanghong.utils.Tools
import java.util.*

class ListAdapterCategory// Provide a suitable constructor (depends on the kind of dataset)
(private val ctx: Context, items: List<Category>) : RecyclerView.Adapter<ListAdapterCategory.ViewHolder>() {

    private var items = ArrayList<Category>()
    private var mOnItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(view: View, obj: Category, position: Int)
    }

    fun setOnItemClickListener(mItemClickListener: (Any, Any:Category, Any) -> Unit) {
        this.mOnItemClickListener = mItemClickListener as? OnItemClickListener
    }

    init {
        this.items = items as ArrayList<Category>
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        // each data item is just a string in this case
        var name: TextView = v.findViewById<View>(R.id.name) as TextView
        var image: ImageView = v.findViewById<View>(R.id.image) as ImageView
        var counter: TextView = v.findViewById<View>(R.id.counter) as TextView
        var lyt_parent: LinearLayout = v.findViewById<View>(R.id.lyt_parent) as LinearLayout

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_category, parent, false)

        // set the view's size, margins, paddings and layout parameters
        val vh = ViewHolder(v)
        return vh
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val c = items[position]
        holder.name.text = c.name
        holder.counter.text = c.note_count.toString() + ""
        holder.image.setImageResource(Tools.StringToResId(c.icon!!, ctx))
        (holder.image.background as GradientDrawable).setColor(Color.parseColor(c.color))
        holder.lyt_parent.setOnClickListener { view -> mOnItemClickListener?.onItemClick(view, c, position) }
    }

    fun getItem(position: Int): Category {
        return items[position]
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return items.size
    }

}