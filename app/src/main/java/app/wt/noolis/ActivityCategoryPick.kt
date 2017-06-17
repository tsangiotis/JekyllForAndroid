package app.wt.noolis

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.CardView
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView

import app.wt.noolis.data.DatabaseManager
import app.wt.noolis.model.Category
import app.wt.noolis.utils.Tools

class ActivityCategoryPick : AppCompatActivity() {

    private var adapterListCategory: AdapterListCategory? = null
    private var db: DatabaseManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_pick)
        db = DatabaseManager(this)
        initToolbar()

        adapterListCategory = AdapterListCategory(this, db!!.allCategory)
        val listView = findViewById(R.id.paired_devices) as ListView
        listView.adapter = adapterListCategory
        listView.onItemClickListener = AdapterView.OnItemClickListener { av, v, i, l -> sendIntentResult(adapterListCategory!!.getItem(i) as Category) }
    }

    private fun initToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(false)
        actionBar.setHomeButtonEnabled(false)
        actionBar.setTitle(R.string.title_activity_pick_category)
    }

    private fun sendIntentResult(category: Category) {
        // send extra object
        val intent = Intent()
        intent.putExtra(EXTRA_OBJ, category)

        // Set result and finish this Activity
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private inner class AdapterListCategory(private val ctx: Context, private val items: List<Category>) : BaseAdapter() {

        override fun getCount(): Int {
            return items.size
        }

        override fun getItem(position: Int): Any {
            return items[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val obj = getItem(position) as Category
            val holder: ViewHolder
            if (convertView == null) {
                holder = ViewHolder()
                convertView = LayoutInflater.from(ctx).inflate(R.layout.row_category_simple, parent, false)
                holder.image = convertView!!.findViewById(R.id.image) as ImageView
                holder.name = convertView.findViewById(R.id.name) as TextView
                convertView.tag = holder
            } else {
                holder = convertView.tag as ViewHolder
            }

            holder.image!!.setImageResource(Tools.StringToResId(obj.icon!!, ctx))
            (holder.image!!.background as GradientDrawable).setColor(Color.parseColor(obj.color))
            holder.name!!.text = obj.name

            return convertView
        }

        private inner class ViewHolder {
            internal var image: ImageView? = null
            internal var name: TextView? = null
        }
    }

    companion object {


        /** Return Intent extra  */
        var EXTRA_OBJ = "app.wt.noolis.EXTRA_OBJ"
    }

}
