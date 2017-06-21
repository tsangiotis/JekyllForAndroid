package com.jchanghong

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
import com.jchanghong.data.Constant
import com.jchanghong.data.DatabaseManager
import com.jchanghong.model.Category
import com.jchanghong.model.Note
import com.jchanghong.utils.Tools
import com.jchanghong.utils.getyam
import com.jchanghong.utils.hasYamHead
import com.jchanghong.utils.removeyam
import gr.tsagi.jekyllforandroid.app.activities.PreviewMarkdownActivity
import gr.tsagi.jekyllforandroid.app.utils.GithubPush
import org.yaml.snakeyaml.Yaml
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutionException

class ActivityNoteEdit : AppCompatActivity() {

  lateinit  private var toolbar: Toolbar
  lateinit  private var actionBar: ActionBar
  lateinit  private var parent_view: View
   lateinit private var tittle: EditText
  lateinit  private var content: EditText
  lateinit  private var time: TextView
  lateinit  private var cat_icon: ImageView
  lateinit  private var cat_drop: ImageView
  lateinit  private var cat_text: TextView
  lateinit  private var appbar: AppBarLayout
   lateinit private var menu: Menu

    private var fav_checked = false
    private var is_new = true

    private var ext_note: Note? = null
    private var cur_category: Category? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)
        parent_view = findViewById(android.R.id.content)

        initToolbar()
        // get extra object
        ext_note = intent.getSerializableExtra(EXTRA_OBJCT) as? Note
        iniComponent()

    }


    private fun initToolbar() {
        toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        actionBar = supportActionBar!!
        actionBar .setDisplayHomeAsUpEnabled(true)
        actionBar .setHomeButtonEnabled(true)
        actionBar .title = ""
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
            time .text = ""
            cur_category = DatabaseManager .defaultCAT
        } else {
            time .text = getString(R.string.time_edited) + Tools.stringToDate(ext_note?.lastEdit?:1)
            tittle .setText(ext_note ?.tittle?:"")
            content .setText(ext_note ?.content?.removeyam()?:"null")
            cur_category = ext_note ?.category
        }
        setCategoryView(cur_category?:DatabaseManager.defaultCAT)

        (findViewById(R.id.lyt_category) as LinearLayout).setOnClickListener {
            val i = Intent(applicationContext, ActivityCategoryPick::class.java)
            startActivityForResult(i, OPEN_DIALOG_CATEGORY_CODE)
        }

        (findViewById(R.id.bt_save) as Button).setOnClickListener { actionSave() }
    }

    private fun setCategoryView(category: Category) {
        cat_icon .setImageResource(Tools.StringToResId(category.icon, applicationContext))
        cat_icon .setColorFilter(Color.parseColor(category.color))
        cat_drop .setColorFilter(Color.parseColor(category.color))
        cat_text .text = category.name
        cat_text .setTextColor(Color.parseColor(category.color))
        appbar .setBackgroundColor(Color.parseColor(category.color))
        Tools.systemBarLolipopCustom(this@ActivityNoteEdit, category.color)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.menu_activity_manage_note, menu)
        //set fav icon
        if (!is_new) {
            if (ext_note ?.favourite == 1) {
                fav_checked = true
                menu.getItem(0).icon = resources.getDrawable(R.drawable.ic_favorites_solid,theme)
            } else {
                fav_checked = false
                menu.getItem(0).icon = resources.getDrawable(R.drawable.ic_favorites_outline,theme)
            }
        } else {
            menu.getItem(0).isVisible = false
            menu.getItem(1).isVisible = false
        }
        return true
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == OPEN_DIALOG_CATEGORY_CODE && resultCode == Activity.RESULT_OK) {
            cur_category = data?.getSerializableExtra(ActivityCategoryPick.EXTRA_OBJ) as? Category
            setCategoryView(cur_category?:DatabaseManager.firstCategory)
            if (Constant.iszhong) {

                Snackbar.make(parent_view, "选择了分类 : " + cur_category?.name, Snackbar.LENGTH_SHORT).show()
            }
            else{

                Snackbar.make(parent_view, "Category Selected : " + cur_category?.name, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.action_delete -> deleteConfirmation()
            R.id.action_fav -> actionFavorite()
            R.id.action_preview-> preview()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun preview() {
            val myIntent = Intent(this, PreviewMarkdownActivity::class.java)
            myIntent.putExtra(PreviewMarkdownActivity.POST_CONTENT,ext_note?.content?.removeyam())
            startActivity(myIntent)
    }

    private fun actionFavorite() {
        if (!is_new) {
            if (fav_checked) {
                menu .getItem(0).icon = resources.getDrawable(R.drawable.ic_favorites_outline,theme)
                DatabaseManager .removeFav(ext_note?.id?:1)
                Snackbar.make(parent_view, getString(R.string.Removedfromfavorites), Snackbar.LENGTH_SHORT).show()
                fav_checked = false
            } else {
                menu .getItem(0).icon = resources.getDrawable(R.drawable.ic_favorites_solid,theme)
                DatabaseManager .setFav(ext_note?.id?:1)
                Snackbar.make(parent_view, getString(R.string.Addedtofavorites), Snackbar.LENGTH_SHORT).show()
                fav_checked = true
            }
        }
    }

    private fun actionSave() {
        if (tittle .text.toString() == "" || content .text.toString() == "") {
            Snackbar.make(parent_view, getString(R.string.Contentcannotbeempty), Snackbar.LENGTH_SHORT).show()
        } else {
            if (is_new) ext_note = Note()
            val notif_text: String

            ext_note ?.tittle = tittle .text.toString()
            ext_note ?.content = content .text.toString()
            ext_note ?.lastEdit = System.currentTimeMillis()
            ext_note ?.category = cur_category!!

            if (is_new) {
                notif_text = getString(R.string.notesaved)
                DatabaseManager .insertNote(ext_note!!)
                pushPost(ext_note!!)
            } else {
                notif_text = getString(R.string.noteupdate)
                DatabaseManager .updateNote(ext_note!!)
                pushPost(ext_note!!)
            }

            Snackbar.make(parent_view, notif_text, Snackbar.LENGTH_SHORT).setCallback(object : Snackbar.Callback() {
                override fun onDismissed(snackbar: Snackbar?, event: Int) {
                    super.onDismissed(snackbar, event)
                    finish()
                }
            }).show()
        }
    }

    fun pushPost(note: Note) {

        val date = SimpleDateFormat("yyyy-MM-dd").format(Date(note.lastEdit))
        val pusher = GithubPush(this)
        if (note.content.hasYamHead()) {
            pusher.pushContent(note.tittle, date, note.content,note.category.name)
        }
        val yaml = Yaml()
//        val customYaml = prefs.getString("yaml_values", "")
//        Log.d("yaml", customYaml)
        var map = yaml.load(note.content.getyam()) as? HashMap<String, Any>
        if (map==null)
        {
            map = HashMap<String, Any>()
            map.put("tags", note.category.name)
        }
//        map.put("tags", note.category.name)
        map.put("category", note.category.name)
        map.put("title", note.tittle)
        map.put("layout", "post")
        val output = "---\n" + yaml.dump(map) + "---\n"

        try {
            pusher.pushContent(note.tittle, date, output + note.content.removeyam(),note.category.name)

        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }


    override fun onBackPressed() {
        if (ext_note != null) {
            if (tittle .text.toString() != ext_note!!.tittle ||
                    content .text.toString() != ext_note!!.content.removeyam() ||
                    cur_category?.id != ext_note!!.category.id) {
                backConfirmation()
            } else {
                finish()
            }
        } else {
            if (tittle .text.toString() == "" && content .text.toString() == "") {
                //do nothing
                finish()
            } else {
                backConfirmation()
            }
        }
    }

    private fun backConfirmation() {
        val builder = AlertDialog.Builder(this@ActivityNoteEdit)
        builder.setTitle(getString(R.string.SaveConfirmation))
        builder.setMessage(getString(R.string.doyouwanttosave))
        builder.setPositiveButton(getString(R.string.yes)) { dialogInterface, i ->
            if (ext_note == null) {
                //save new note
                val n = Note()
                n.tittle = tittle .text.toString() + ""
                n.content = content .text.toString() + ""
                n.category = cur_category!!
                n.favourite = 0
                n.lastEdit = System.currentTimeMillis()
                DatabaseManager .insertNote(n)
                pushPost(n)
            } else {
                ext_note?.tittle = tittle .text.toString() + ""
                ext_note?.content = content .text.toString() + ""
                ext_note?.category = cur_category!!
                //no need to set fav here, fav already save to DB when clicked
                ext_note?.lastEdit = System.currentTimeMillis()
                DatabaseManager .updateNote(ext_note!!)
                pushPost(ext_note!!)
                ext_note?.clear()
            }
            Snackbar.make(parent_view, getString(R.string.notesaved), Snackbar.LENGTH_SHORT).show()
            finish()
        }
        builder.setNegativeButton("No") { dialogInterface, i -> finish() }
        builder.show()
    }

    private fun deleteConfirmation() {
        val builder = AlertDialog.Builder(this@ActivityNoteEdit)
        builder.setTitle(getString(R.string.SaveConfirmation1))
        builder.setMessage(getString(R.string.areyouwantdelete))
        builder.setPositiveButton(getString(R.string.yes)) { dialogInterface, i ->
            if (ext_note != null) {//modify
                DatabaseManager .deleteNote(ext_note?.id?:1)
            }
            Toast.makeText(applicationContext, getString(R.string.notedeleted), Toast.LENGTH_SHORT).show()
            finish()
        }
        builder.setNegativeButton(getString(R.string.no), null)
        builder.show()
    }

    companion object {

        val EXTRA_OBJCT = "com.jchanghong.EXTRA_OBJECT_NOTE"
        val OPEN_DIALOG_CATEGORY_CODE = 1
    }

}
