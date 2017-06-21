package com.jchanghong.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import com.jchanghong.R
import com.jchanghong.model.CategoryIcon
import com.jchanghong.utils.Tools

/**
 * \* Created with IntelliJ IDEA.
 * \* User: jchanghong
 * \* Date: 17/06/2016
 * \* Time: 10:00
 * \ */
class ListAdapterCategoryIcon(private val context: Context, private val categoryIconList: List<CategoryIcon>) : RecyclerView.Adapter<ListAdapterCategoryIcon.ViewHolder>() {
    var selectedCategoryIcon: CategoryIcon? = null
        private set
    private var clickedPos = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_category_icon, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val icon = categoryIconList[position]
        holder.vIcon.setImageResource(Tools.StringToResId(icon.icon, context))
        (holder.vIcon.background as GradientDrawable).setColor(Color.parseColor(icon.color))
        holder.radioButton.isChecked = icon.isChecked
        holder.radioButton.tag = position


        if (position == 0 && categoryIconList[0].isChecked && holder.radioButton.isChecked) {
            lastChecked = holder.radioButton
            lastCheckedPos = 0
        } else if (holder.radioButton.isChecked) {
            lastChecked = holder.radioButton
        }

        holder.radioButton.setOnClickListener { v ->
            val cb = v as RadioButton
            clickedPos = (cb.tag as Int).toInt()

            if (cb.isChecked) {

                if (lastChecked != null) {
                    if (clickedPos != lastCheckedPos) {
                        lastChecked!!.isChecked = false
                        categoryIconList[lastCheckedPos].isChecked = false
                    }
                }

                lastChecked = cb
                lastCheckedPos = clickedPos

            } else {
                lastChecked = null
            }

            categoryIconList[clickedPos].isChecked = cb.isChecked
            selectedCategoryIcon = categoryIconList[lastCheckedPos]
        }
    }

    override fun getItemCount(): Int {
        return categoryIconList.size
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var vIcon: ImageView = v.findViewById<View>(R.id.image_icon) as ImageView
        var radioButton: RadioButton = v.findViewById<View>(R.id.radioSelected) as RadioButton


    }

    fun setSelectedRadio(icon: String) {
        val pos = categoryIconList.indices.lastOrNull { categoryIconList[it].icon == icon }
                ?: 0
        clickedPos = pos
        lastCheckedPos = pos
        categoryIconList[pos].isChecked = true
        selectedCategoryIcon = categoryIconList[pos]
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var lastChecked: RadioButton? = null
        private var lastCheckedPos = 0
    }
}
