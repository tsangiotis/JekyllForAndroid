package app.wt.noolis.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast

import app.wt.noolis.R
import app.wt.noolis.model.CategoryIcon
import app.wt.noolis.utils.Tools

/**
 * Created by Kodok on 17/06/2016.
 */
class ListAdapterCategoryIcon(private val context: Context, private val categoryIconList: List<CategoryIcon>) : RecyclerView.Adapter<ListAdapterCategoryIcon.ViewHolder>() {
    private val checked: BooleanArray
    var selectedCategoryIcon: CategoryIcon? = null
        private set
    private var clickedPos = 0

    init {
        checked = BooleanArray(categoryIconList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_category_icon, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val icon = categoryIconList[position]
        holder.vIcon.setImageResource(Tools.StringToResId(icon.icon!!, context))
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
        var vIcon: ImageView
        var radioButton: RadioButton
        protected var lyt_parent: LinearLayout

        init {
            vIcon = v.findViewById(R.id.image_icon) as ImageView
            radioButton = v.findViewById(R.id.radioSelected) as RadioButton
            lyt_parent = v.findViewById(R.id.lyt_parent) as LinearLayout
        }


    }

    fun setSelectedRadio(icon: String) {
        var pos = 0
        for (i in categoryIconList.indices) {
            if (categoryIconList[i].icon == icon) {
                pos = i
            }
        }
        clickedPos = pos
        lastCheckedPos = pos
        categoryIconList[pos].isChecked = true
        selectedCategoryIcon = categoryIconList[pos]
    }

    companion object {
        private var lastChecked: RadioButton? = null
        private var lastCheckedPos = 0
    }
}
