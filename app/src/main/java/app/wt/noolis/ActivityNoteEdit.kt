package app.wt.noolis

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import app.wt.noolis.data.DatabaseManager
import app.wt.noolis.model.Category
import app.wt.noolis.model.Note
import app.wt.noolis.utils.Tools

class ActivityNoteEdit : AppCompatActivity() {

    private var toolbar: Toolbar? = null
    private var actionBar: ActionBar? = null
    private var parent_view: View? = null
    private var tittle: EditText? = null
    private var content: EditText? = null
    private var time: TextView? = null
    private var cat_icon: ImageView? = null
    private var cat_drop: ImageView? = null
    private var cat_text: TextView? = null
    private var appbar: AppBarLayout? = null
    private var menu: Menu? = null

    private var fav_checked = false
    private var is_new = true

    private var ext_note: Note? = null
    private var cur_category: Category? = null
    private var db: DatabaseManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)
        parent_view = findViewById(android.R.id.content)

        // init db
        db = DatabaseManager(this)

        initToolbar()

        // get extra object
        ext_note = intent.getSerializableExtra(EXTRA_OBJCT) as Note
        iniComponent()

    }


    private fun initToolbar() {
        toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setHomeButtonEnabled(true)
        actionBar!!.title = ""
    }


    private fun iniComponent() {
        tittle = findViewById(R.id.tittle) as EditText
        content = findViewById(R.id.content) as EditText
        time = findViewById(R.id.time) as TextView

        cat_icon = findViewById(R.id.cat_icon) as ImageView
        cat_drop = findViewById(R.id.cat_drop) as ImageView
        cat_text = findViewById(R.id.cat_text) as TextView
        appbar = findViewById(R.id.appbar) as AppBarLayout

        is_new = ext_note == null

        if (is_new) {
            time!!.text = ""
            cur_category = db!!.firstCategory
        } else {
            time!!.text = "Edited : " + Tools.stringToDate(ext_note!!.lastEdit)
            tittle!!.setText(ext_note!!.tittle)
            content!!.setText(ext_note!!.content)
            cur_category = ext_note!!.category
        }
        setCategoryView(cur_category!!)

        (findViewById(R.id.lyt_category) as LinearLayout).setOnClickListener {
            val i = Intent(applicationContext, ActivityCategoryPick::class.java)
            startActivityForResult(i, OPEN_DIALOG_CATEGORY_CODE)
        }

        (findViewById(R.id.bt_save) as Button).setOnClickListener { actionSave() }
    }

    private fun setCategoryView(category: Category) {
        cat_icon!!.setImageResource(Tools.StringToResId(category.icon!!, applicationContext))
        cat_icon!!.setColorFilter(Color.parseColor(category.color))
        cat_drop!!.setColorFilter(Color.parseColor(category.color))
        cat_text!!.text = category.name
        cat_text!!.setTextColor(Color.parseColor(category.color))
        appbar!!.setBackgroundColor(Color.parseColor(category.color))
        Tools.systemBarLolipopCustom(this@ActivityNoteEdit, category.color!!)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.menu_activity_manage_note, menu)
        //set fav icon
        if (!is_new) {
            if (ext_note!!.favourite == 1) {
                fav_checked = true
                menu.getItem(0).icon = resources.getDrawable(R.drawable.ic_favorites_solid)
            } else {
                fav_checked = false
                menu.getItem(0).icon = resources.getDrawable(R.drawable.ic_favorites_outline)
            }
        } else {
            menu.getItem(0).isVisible = false
            menu.getItem(1).isVisible = false
        }
        return true
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == OPEN_DIALOG_CATEGORY_CODE && resultCode == Activity.RESULT_OK) {
            cur_category = data.getSerializableExtra(ActivityCategoryPick.EXTRA_OBJ) as Category
            setCategoryView(cur_category!!)
            Snackbar.make(parent_view!!, "Category Selected : " + cur_category!!.name, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.action_delete -> deleteConfirmation()
            R.id.action_fav -> actionFavorite()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun actionFavorite() {
        if (!is_new) {
            if (fav_checked) {
                menu!!.getItem(0).icon = resources.getDrawable(R.drawable.ic_favorites_outline)
                db!!.removeFav(ext_note!!.id)
                Snackbar.make(parent_view!!, "Removed from favorites", Snackbar.LENGTH_SHORT).show()
                fav_checked = false
            } else {
                menu!!.getItem(0).icon = resources.getDrawable(R.drawable.ic_favorites_solid)
                db!!.setFav(ext_note!!.id)
                Snackbar.make(parent_view!!, "Added to favorites", Snackbar.LENGTH_SHORT).show()
                fav_checked = true
            }
        }
    }

    private fun actionSave() {
        if (tittle!!.text.toString() == "" || content!!.text.toString() == "") {
            Snackbar.make(parent_view!!, "Tittle or Content can't be empty", Snackbar.LENGTH_SHORT).show()
        } else {
            if (is_new) ext_note = Note()
            val notif_text: String

            ext_note!!.tittle = tittle!!.text.toString()
            ext_note!!.content = content!!.text.toString()
            ext_note!!.lastEdit = System.currentTimeMillis()
            ext_note!!.category = cur_category

            if (is_new) {
                notif_text = "Note Saved"
                db!!.insertNote(ext_note!!)
            } else {
                notif_text = "Note Updated"
                db!!.updateNote(ext_note!!)
            }

            Snackbar.make(parent_view!!, notif_text, Snackbar.LENGTH_SHORT).setCallback(object : Snackbar.Callback() {
                override fun onDismissed(snackbar: Snackbar?, event: Int) {
                    super.onDismissed(snackbar, event)
                    finish()
                }
            }).show()
        }
    }

    override fun onBackPressed() {
        if (ext_note != null) {
            if (tittle!!.text.toString() != ext_note!!.tittle ||
                    content!!.text.toString() != ext_note!!.content ||
                    cur_category!!.id != ext_note!!.category!!.id) {
                backConfirmation()
            } else {
                finish()
            }
        } else {
            if (tittle!!.text.toString() == "" && content!!.text.toString() == "") {
                //do nothing
                finish()
            } else {
                backConfirmation()
            }
        }
    }

    private fun backConfirmation() {
        val builder = AlertDialog.Builder(this@ActivityNoteEdit)
        builder.setTitle("Save Confirmation")
        builder.setMessage("Do you want to save?")
        builder.setPositiveButton("Yes") { dialogInterface, i ->
            if (ext_note == null) {
                //save new note
                val n = Note()
                n.tittle = tittle!!.text.toString() + ""
                n.content = content!!.text.toString() + ""
                n.category = cur_category
                n.favourite = 0
                n.lastEdit = System.currentTimeMillis()
                db!!.insertNote(n)
            } else {
                ext_note!!.tittle = tittle!!.text.toString() + ""
                ext_note!!.content = content!!.text.toString() + ""
                ext_note!!.category = cur_category
                //no need to set fav here, fav already save to DB when clicked
                ext_note!!.lastEdit = System.currentTimeMillis()
                db!!.updateNote(ext_note!!)
                ext_note!!.clear()
            }
            Snackbar.make(parent_view!!, "Note Saved", Snackbar.LENGTH_SHORT).show()
            finish()
        }
        builder.setNegativeButton("No") { dialogInterface, i -> finish() }
        builder.show()
    }

    private fun deleteConfirmation() {
        val builder = AlertDialog.Builder(this@ActivityNoteEdit)
        builder.setTitle("Delete Confirmation")
        builder.setMessage("Are you sure want to delete this Note?")
        builder.setPositiveButton("Yes") { dialogInterface, i ->
            if (ext_note != null) {//modify
                db!!.deleteNote(ext_note!!.id)
            }
            Toast.makeText(applicationContext, "Note Deleted", Toast.LENGTH_SHORT).show()
            finish()
        }
        builder.setNegativeButton("No", null)
        builder.show()
    }

    companion object {

        val EXTRA_OBJCT = "app.wt.noolis.EXTRA_OBJECT_NOTE"
        val OPEN_DIALOG_CATEGORY_CODE = 1
    }

}
