package app.wt.noolis

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import app.wt.noolis.adapter.ListAdapterNote
import app.wt.noolis.data.DatabaseManager
import app.wt.noolis.model.Category
import app.wt.noolis.model.Note
import app.wt.noolis.utils.Tools

class ActivityCategoryDetails : AppCompatActivity() {

    private var toolbar: Toolbar? = null
    private var actionBar: ActionBar? = null
    private var menu: Menu? = null

    private var image: ImageView? = null
    private var name: TextView? = null
    private var appbar: AppBarLayout? = null
    private var ext_category: Category? = null

  lateinit  var recyclerView: RecyclerView
  lateinit  var mAdapter: ListAdapterNote
    private var lyt_not_found: LinearLayout? = null
    private var db: DatabaseManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_details)

        // init db
        db = DatabaseManager(this)

        // get extra object
        ext_category = intent.getSerializableExtra(EXTRA_OBJCT) as Category
        iniComponent()
        if (ext_category != null) {
            setCategoryView()
        }
        initToolbar()

        recyclerView = findViewById(R.id.recyclerView) as RecyclerView
        lyt_not_found = findViewById(R.id.lyt_not_found) as LinearLayout

        recyclerView.layoutManager = LinearLayoutManager(this@ActivityCategoryDetails)
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()


        displayData(db!!.getNotesByCategoryId(ext_category!!.id))
    }

    private fun iniComponent() {
        image = findViewById(R.id.image) as ImageView
        name = findViewById(R.id.name) as TextView
        appbar = findViewById(R.id.appbar) as AppBarLayout
    }

    private fun setCategoryView() {
        image!!.setImageResource(Tools.StringToResId(ext_category!!.icon?:"null", applicationContext))
        image!!.setColorFilter(Color.parseColor(ext_category!!.color))
        name!!.text = ext_category!!.name
        appbar!!.setBackgroundColor(Color.parseColor(ext_category!!.color))
        Tools.systemBarLolipopCustom(this@ActivityCategoryDetails, ext_category!!.color?:"null")
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setHomeButtonEnabled(true)
        actionBar!!.title = ""
    }

    private fun displayData(items: List<Note>) {
        mAdapter = ListAdapterNote(applicationContext, items)
        recyclerView.adapter = mAdapter
//        mAdapter.setOnItemClickListener(object :ListAdapterNote.OnItemClickListener{
//            override fun onItemClick(view: View, model: Note) {
//                val intent = Intent(applicationContext, ActivityNoteEdit::class.java)
//                intent.putExtra(ActivityNoteEdit.EXTRA_OBJCT, model)
//                startActivity(intent)
//            }
//        })
        mAdapter.setOnItemClickListener {

            _, model ->
            val intent = Intent(applicationContext, ActivityNoteEdit::class.java)
            intent.putExtra(ActivityNoteEdit.EXTRA_OBJCT, model)
            startActivity(intent)
        }
        if (mAdapter.itemCount == 0) {
            lyt_not_found!!.visibility = View.VISIBLE
        } else {
            lyt_not_found!!.visibility = View.GONE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.action_edit_cat -> {
                val i = Intent(applicationContext, ActivityCategoryEdit::class.java)
                i.putExtra(ActivityCategoryDetails.EXTRA_OBJCT, ext_category)
                startActivity(i)
            }
            R.id.action_delete_cat -> if (db!!.getNotesByCategoryId(ext_category!!.id).isEmpty()) {
                //                    db.deleteCategory(ext_category.getId());
                //                    Toast.makeText(getApplicationContext(),"Category deleted", Toast.LENGTH_SHORT).show();
                deleteConfirmation()
                //                    finish();
            } else {
                Snackbar.make(recyclerView, "Category is not empty", Snackbar.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.menu_activity_category, menu)
        return true
    }

    override fun onResume() {
        super.onResume()
        ext_category = db!!.getCategoryById(ext_category!!.id)
        recyclerView.layoutManager = LinearLayoutManager(this@ActivityCategoryDetails)
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()

        image!!.setImageResource(Tools.StringToResId(ext_category!!.icon?:"", applicationContext))
        image!!.setColorFilter(Color.parseColor(ext_category!!.color))
        name!!.text = ext_category!!.name
        appbar!!.setBackgroundColor(Color.parseColor(ext_category!!.color))
        Tools.systemBarLolipopCustom(this@ActivityCategoryDetails, ext_category!!.color?:"")

        displayData(db!!.getNotesByCategoryId(ext_category!!.id))
    }

    private fun deleteConfirmation() {
        val builder = AlertDialog.Builder(this@ActivityCategoryDetails)
        builder.setTitle("Delete Confirmation")
        builder.setMessage("Are you sure want to delete this Category?")
        builder.setPositiveButton("Yes") { _, _ ->
            if (ext_category != null) {
                db!!.deleteCategory(ext_category!!.id)
            }
            Toast.makeText(applicationContext, "Category Deleted", Toast.LENGTH_SHORT).show()
            finish()
        }
        builder.setNegativeButton("No", null)
        builder.show()
    }

    companion object {

        val EXTRA_OBJCT = "app.wt.noolis.EXTRA_OBJECT_CATEGORY"
    }
}
