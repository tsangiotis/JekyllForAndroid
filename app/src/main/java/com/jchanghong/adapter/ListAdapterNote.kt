package com.jchanghong.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.jchanghong.R
import com.jchanghong.model.Note
import com.jchanghong.utils.Tools
import java.util.*


class ListAdapterNote(private val context: Context, items: List<Note>) : RecyclerView.Adapter<ListAdapterNote.ViewHolder>(), Filterable {

    private var original_items = ArrayList<Note>()
    private var filtered_items: List<Note> = ArrayList()
    private val mFilter = ItemFilter()

    private var onItemClickListener: OnItemClickListener? = null

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var title: TextView = v.findViewById<View>(R.id.title) as TextView
        var content: TextView = v.findViewById<View>(R.id.content) as TextView
        var time: TextView = v.findViewById<View>(R.id.time) as TextView
        var image: ImageView = v.findViewById<View>(R.id.image) as ImageView
        var lyt_parent: LinearLayout = v.findViewById<View>(R.id.lyt_parent) as LinearLayout

    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    init {
        original_items = items as ArrayList<Note>
        filtered_items = items
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_note, parent, false)
        // set the view's size, margins, paddings and layout parameters
        val vh = ViewHolder(v)
        return vh
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val n = filtered_items[position]
        holder.title.text = n.tittle
        holder.time.text = Tools.stringToDate(n.lastEdit)
        holder.content.text = n.content
        holder.image.setImageResource(Tools.StringToResId(n.category.icon, context))
        (holder.image.background as GradientDrawable).setColor(Color.parseColor(n.category.color))

        holder.lyt_parent.setOnClickListener { v -> onItemClickListener!!.onItemClick(v, n) }
    }

    override fun getItemCount(): Int {
        return filtered_items.size
    }


    interface OnItemClickListener {
        fun onItemClick(view: View, model: Note)
    }

    override fun getFilter(): Filter {
        return mFilter
    }

    private inner class ItemFilter : Filter() {
        override fun performFiltering(constraint: CharSequence): Filter.FilterResults {
            val query = constraint.toString().toLowerCase()

            val results = Filter.FilterResults()
            val list = original_items
            val result_list = ArrayList<Note>(list.size)

            for (i in list.indices) {
                val str_title = list[i].tittle
                val str_content = list[i].content
                if (str_title.toLowerCase().contains(query) || str_content.toLowerCase().contains(query)) {
                    result_list.add(list[i])
                }
            }

            results.values = result_list
            results.count = result_list.size

            return results
        }

        override fun publishResults(constraint: CharSequence, results: Filter.FilterResults) {
            filtered_items = results.values as List<Note>
            notifyDataSetChanged()
        }

    }
}
