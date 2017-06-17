package app.wt.noolis.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import app.wt.noolis.R
import app.wt.noolis.model.Category
import app.wt.noolis.model.CategoryIcon
import app.wt.noolis.model.Note
import app.wt.noolis.utils.Tools
import java.util.*

class DatabaseManager(private val context: Context) : SQLiteOpenHelper(context, DatabaseManager.DB_NAME, null, DatabaseManager.DB_VERSION) {

    private val cat_id: IntArray = context.resources.getIntArray(R.array.category_id)
    private val cat_name: Array<String>
    private val cat_color: Array<String>
    private val cat_icon: Array<String>
    private val cat_icon_data: Array<String>
    private val cat_color_data: Array<String>

    init {

        cat_name = context.resources.getStringArray(R.array.category_name)
        cat_color = context.resources.getStringArray(R.array.category_color)
        cat_icon = context.resources.getStringArray(R.array.category_icon)
        cat_icon_data = context.resources.getStringArray(R.array.category_icon_data)
        cat_color_data = context.resources.getStringArray(R.array.category_color_data)
    }

    override fun onCreate(db: SQLiteDatabase) {
        createTableNote(db)
        createTableCategory(db)
        createTableCategoryIcon(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion == 1 && newVersion == 2) {
            val categories = getAllCategoryVersion3(db)
            Log.e("onUpgrade", "MASUK 1 2")
            val notes = getAllNotesVersion3(db)

            deleteCategoryIconTable(db)
            deleteNoteTable(db)
            deleteCategoryTable(db)

            createTableCategoryIconVersion3(db)
            createTableCategoryVersion3(db)
            createTableNoteVersion3(db)

            defineCategoryIconVersion3(db)
            insertCategoryVersion3(db, redefineCategoryVersion3(categories))
            insertNoteVerion3(db, notes)
        }

    }

    private fun createTableNote(db: SQLiteDatabase) {
        val CREATE_TABLE = """"CREATE TABLE $TABLE_NOTE ("
        +COL_N_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        +COL_N_TITLE + " TEXT, "
        +COL_N_CONTENT + " TEXT, "
        +COL_N_FAV + " INTEGER, "
        +COL_N_LAST_EDIT + " NUMERIC, "
        +COL_N_CATEGORY + " INTEGER, "
        +" FOREIGN KEY(" + COL_N_CATEGORY + ") REFERENCES " + TABLE_NOTE + "(" + COL_C_ID + ")"
        +")"""
        try {
            db.execSQL(CREATE_TABLE)
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        }

    }

    private fun createTableCategory(db: SQLiteDatabase) {
        val CREATE_TABLE = """CREATE TABLE $TABLE_CATEGORY ("
        +COL_C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        +COL_C_NAME + " TEXT, "
        +COL_C_COLOR + " TEXT, "
        +COL_C_ICON + " TEXT "
        +" )"""
        try {
            db.execSQL(CREATE_TABLE)
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        }

    }

    fun defineCategory() {
        val db = this.writableDatabase
        for (i in cat_id.indices) {
            val values = ContentValues()
            //values.put(COL_C_ID, cat_id[i]);
            values.put(COL_C_NAME, cat_name[i])
            values.put(COL_C_COLOR, cat_color[i])
            values.put(COL_C_ICON, cat_icon[i])
            Log.e("ICON : ", i.toString() + " | " + cat_icon[i])
            db.insert(TABLE_CATEGORY, null, values) // Inserting Row
        }
        db.close()
    }

    private fun createTableCategoryIcon(db: SQLiteDatabase) {
        val CREATE_TABLE = """CREATE TABLE $TABLE_CATEGORY_ICON ("
        +COL_C_ID + " INTEGER PRIMARY KEY , "
        +COL_C_ICON + " TEXT, "
        +COL_C_COLOR + " TEXT "
        +" )"""
        try {
            db.execSQL(CREATE_TABLE)
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        }

    }

    fun defineCategoryIcon() {
        val db = this.writableDatabase
        for (i in cat_icon_data.indices) {
            val values = ContentValues()
            values.put(COL_C_ICON, cat_icon_data[i])
            values.put(COL_C_COLOR, cat_color_data[i])
            Log.e("ICON DATA: ", i.toString() + " | " + cat_icon_data[i])
            db.insert(TABLE_CATEGORY_ICON, null, values) // Inserting Row
        }
        db.close()
    }


    /**
     * All Note transaction
     */
    fun insertNote(note: Note) {
        val values = ContentValues()
        values.put(COL_N_TITLE, note.tittle)
        values.put(COL_N_CONTENT, note.content)
        values.put(COL_N_FAV, note.favourite)
        values.put(COL_N_LAST_EDIT, note.lastEdit)
        values.put(COL_N_CATEGORY, note.category!!.id)
        val db = this.writableDatabase
        try {
            db.insert(TABLE_NOTE, null, values)
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        } finally {
            db.close()
        }
    }

    fun deleteNote(rowId: Long) {
        val db = this.writableDatabase
        try {
            db.delete(TABLE_NOTE, COL_N_ID + "=" + rowId, null)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Db Error", e.toString())
        } finally {
            db.close()
        }
    }

    fun updateNote(note: Note) {
        val contentValues = ContentValues()
        contentValues.put(COL_N_TITLE, note.tittle)
        contentValues.put(COL_N_CONTENT, note.content)
        contentValues.put(COL_N_LAST_EDIT, note.lastEdit)
        contentValues.put(COL_N_CATEGORY, note.category!!.id)
        val db = this.writableDatabase
        try {
            db.update(TABLE_NOTE, contentValues, COL_N_ID + "=" + note.id, null)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Db Error", e.toString())
        } finally {
            db.close()
        }
    }

    operator fun get(id: Long?): Note {
        val db = this.readableDatabase
        var note = Note()
        var cur: Cursor? = null
        try {
            cur = db.rawQuery("SELECT * FROM $TABLE_NOTE WHERE $COL_N_ID = ?", arrayOf(id!!.toString() + ""))
            cur!!.moveToFirst()
            note = getNoteFromCursor(cur)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Db Error", e.toString())
        } finally {
            db.close()
        }
        return note
    }

    val allNotes: List<Note>
        get() {
            val notes = ArrayList<Note>()
            val db = this.readableDatabase
            var cur: Cursor? = null
            try {
                cur = db.rawQuery("SELECT * FROM $TABLE_NOTE ORDER BY $COL_N_ID DESC", null)
                cur!!.moveToFirst()
                if (!cur.isAfterLast) {
                    do {
                        notes.add(getNoteFromCursor(cur))
                    } while (cur.moveToNext())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("DB ERROR", e.toString())
            } finally {
                cur!!.close()
                db.close()
            }
            return notes
        }

    fun getNotesByCategoryId(cat_id: Long?): List<Note> {
        val notes = ArrayList<Note>()
        var cur: Cursor? = null
        val db = this.readableDatabase
        try {
            cur = db.rawQuery("SELECT * FROM $TABLE_NOTE WHERE $COL_N_CATEGORY = ?", arrayOf(cat_id!!.toString() + ""))
            cur!!.moveToFirst()
            if (!cur.isAfterLast) {
                do {
                    notes.add(getNoteFromCursor(cur))
                } while (cur.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("DB ERROR", e.toString())
        } finally {
            cur!!.close()
            db.close()
        }
        return notes
    }

    fun getNotesCountByCategoryId(cat_id: Long?): Int {
        var returnValue = 0
        var cursor: Cursor? = null
        val db = this.readableDatabase
        try {
            cursor = db.rawQuery("SELECT COUNT($COL_N_ID) FROM $TABLE_NOTE WHERE $COL_N_CATEGORY = ?", arrayOf(cat_id!!.toString() + ""))
            cursor!!.moveToFirst()
            returnValue = cursor.getInt(0)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("DB ERROR", e.toString())
        } finally {
            cursor!!.close()
            db.close()
        }
        return returnValue
    }

    private fun getNoteFromCursor(cur: Cursor): Note {
        val n = Note()
        n.id = cur.getLong(0)
        n.tittle = cur.getString(1)
        n.content = cur.getString(2)
        n.favourite = cur.getInt(3)
        n.lastEdit = cur.getLong(4)
        n.category = getCategoryById(cur.getLong(5))
        return n
    }

    /**
     * All Category transaction
     */

    fun getCategoryById(id: Long): Category {
        var category = Category()
        var cur: Cursor? = null
        val db = this.readableDatabase
        try {
            cur = db.rawQuery("SELECT * FROM $TABLE_CATEGORY WHERE $COL_C_ID = ?", arrayOf(id.toString() + ""))
            cur!!.moveToFirst()
            category = getCategoryByCursor(cur)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Db Error", e.toString())
        } finally {
            cur!!.close()
            db.close()
        }
        return category
    }

    val allCategory: List<Category>
        get() {
            val categories = ArrayList<Category>()
            var cur: Cursor? = null
            val db = this.readableDatabase
            try {
                cur = db.rawQuery("SELECT * FROM " + TABLE_CATEGORY, null)
                cur!!.moveToFirst()
                if (!cur.isAfterLast) {
                    do {
                        categories.add(getCategoryByCursor(cur))
                    } while (cur.moveToNext())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Db Error", e.toString())
            } finally {
                cur!!.close()
                db.close()
            }
            return categories
        }

    private fun getCategoryByCursor(cur: Cursor): Category {
        val c = Category()
        c.id = cur.getLong(0)
        c.name = cur.getString(1)
        c.color = cur.getString(2)
        c.icon = cur.getString(3)
        c.note_count = getNotesCountByCategoryId(c.id)
        return c
    }

    val categoryIcon: List<CategoryIcon>
        get() {
            val categoryIcons = ArrayList<CategoryIcon>()
            var cur: Cursor? = null
            val db = this.readableDatabase
            try {
                cur = db.rawQuery("SELECT * FROM " + TABLE_CATEGORY_ICON, null)
                cur!!.moveToFirst()
                if (!cur.isAfterLast) {
                    do {
                        categoryIcons.add(getCategoryIconByCursor(cur))
                    } while (cur.moveToNext())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Db Error", e.toString())
            } finally {
                cur!!.close()
                db.close()
            }
            return categoryIcons
        }

    private fun getCategoryIconByCursor(cur: Cursor): CategoryIcon {
        val ci = CategoryIcon()
        ci.icon = cur.getString(1)
        ci.color = cur.getString(2)
        return ci
    }

    fun insertCategory(category: Category) {
        val values = ContentValues()
        values.put(COL_C_NAME, category.name)
        values.put(COL_C_COLOR, category.color)
        values.put(COL_C_ICON, category.icon)
        val db = this.writableDatabase
        try {
            db.insert(TABLE_CATEGORY, null, values) // Inserting Row
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        } finally {
            db.close()
        }
    }

    fun updateCategory(category: Category) {
        val contentValues = ContentValues()
        contentValues.put(COL_C_ICON, category.icon)
        contentValues.put(COL_C_COLOR, category.color)
        contentValues.put(COL_C_NAME, category.name)
        val db = this.writableDatabase
        try {
            db.update(TABLE_CATEGORY, contentValues, COL_C_ID + "=" + category.id, null)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Db Error", e.toString())
        } finally {
            db.close()
        }
    }

    fun deleteCategory(rowId: Long) {
        val db = this.writableDatabase
        try {
            db.delete(TABLE_CATEGORY, COL_C_ID + "=" + rowId, null)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Db Error", e.toString())
        } finally {
            db.close()
        }
    }

    val firstCategory: Category
        get() {
            var category = Category()
            var cur: Cursor? = null
            val db = this.readableDatabase
            try {
                cur = db.rawQuery("SELECT * FROM $TABLE_CATEGORY ORDER BY ROWID ASC LIMIT 1 ", null)
                cur!!.moveToFirst()
                category = getCategoryByCursor(cur)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Db Error", e.toString())
            } finally {
                cur!!.close()
                db.close()
            }
            return category
        }


    /**
     * All Favorites Note transaction
     */
    val allFavNote: List<Note>
        get() {
            val notes = ArrayList<Note>()
            var cur: Cursor? = null
            val db = this.readableDatabase
            try {
                cur = db.rawQuery("SELECT * FROM $TABLE_NOTE WHERE $COL_N_FAV = ? ORDER BY $COL_N_ID DESC", arrayOf("1"))
                cur!!.moveToFirst()
                if (!cur.isAfterLast) {
                    do {
                        notes.add(getNoteFromCursor(cur))
                    } while (cur.moveToNext())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("DB ERROR", e.toString())
            } finally {
                cur!!.close()
                db.close()
            }
            return notes
        }


    fun setFav(id: Long) {
        val contentValues = ContentValues()
        contentValues.put(COL_N_FAV, 1)
        val db = this.writableDatabase
        try {
            db.update(TABLE_NOTE, contentValues, COL_N_ID + "=" + id, null)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Db Error", e.toString())
        } finally {
            db.close()
        }
    }

    fun removeFav(id: Long) {
        if (isFavoriteExist(id)) {
            val contentValues = ContentValues()
            contentValues.put(COL_N_FAV, 0)
            val db = this.writableDatabase
            try {
                db.update(TABLE_NOTE, contentValues, COL_N_ID + "=" + id, null)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Db Error", e.toString())
            } finally {
                db.close()
            }
        }
    }


    /**
     * Support method
     */
    private fun isFavoriteExist(id: Long): Boolean {
        var cursor: Cursor? = null
        var count = 0
        val db = this.readableDatabase
        try {
            cursor = db.rawQuery("SELECT * FROM $TABLE_NOTE WHERE $COL_N_ID = ?", arrayOf(id.toString() + ""))
            count = cursor!!.count
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Db Error", e.toString())
        } finally {
            cursor!!.close()
            db.close()
        }
        return count > 0
    }

    /**
     * This is only for update 3.0
     */

    fun getAllCategoryVersion3(db: SQLiteDatabase): List<Category> {
        val categories = ArrayList<Category>()
        var cur: Cursor? = null
        try {
            cur = db.rawQuery("SELECT * FROM " + TABLE_CATEGORY, null)
            cur!!.moveToFirst()
            if (!cur.isAfterLast) {
                do {
                    categories.add(getCategoryByCursorVersion3(db, cur))
                } while (cur.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Db Error", e.toString())
        } finally {
            cur!!.close()
        }
        return categories
    }

    private fun getCategoryByCursorVersion3(db: SQLiteDatabase, cur: Cursor): Category {
        val c = Category()
        c.id = cur.getLong(0)
        c.name = cur.getString(1)
        c.color = cur.getString(2)
        c.icon = cur.getString(3)
        c.note_count = getNotesCountByCategoryIdVersion3(db, c.id)
        return c
    }

    fun getNotesCountByCategoryIdVersion3(db: SQLiteDatabase, cat_id: Long?): Int {
        var returnValue = 0
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery("SELECT COUNT($COL_N_ID) FROM $TABLE_NOTE WHERE $COL_N_CATEGORY = ?", arrayOf(cat_id!!.toString() + ""))
            cursor!!.moveToFirst()
            returnValue = cursor.getInt(0)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("DB ERROR", e.toString())
        } finally {
            cursor!!.close()
        }
        return returnValue
    }

    fun getAllNotesVersion3(db: SQLiteDatabase): List<Note> {
        val notes = ArrayList<Note>()
        var cur: Cursor? = null
        try {
            cur = db.rawQuery("SELECT * FROM $TABLE_NOTE ORDER BY $COL_N_ID DESC", null)
            cur!!.moveToFirst()
            if (!cur.isAfterLast) {
                do {
                    notes.add(getNoteFromCursorVersion3(db, cur))
                } while (cur.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("DB ERROR", e.toString())
        } finally {
            cur!!.close()
        }
        return notes
    }

    private fun getNoteFromCursorVersion3(db: SQLiteDatabase, cur: Cursor): Note {
        val n = Note()
        n.id = cur.getLong(0)
        n.tittle = cur.getString(1)
        n.content = cur.getString(2)
        n.favourite = cur.getInt(3)
        n.lastEdit = cur.getLong(4)
        n.category = Category(cur.getLong(5), "", "", "")
        return n
    }

    private fun deleteNoteTable(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTE)
    }

    private fun deleteCategoryTable(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY)
    }

    private fun deleteCategoryIconTable(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY_ICON)
    }

    private fun redefineCategoryVersion3(categories: List<Category>): List<Category> {
        //this method is for check is the icon available or not,if not the icon will be replaced with default icon
        var defaultIcon = cat_icon_data[0]
        var ic = ""
        for (category in categories) {
            for (i in cat_icon_data.indices) {
                ic = Tools.StringToResId(cat_icon_data[i], context).toString()
                if (ic == category.icon) {
                    defaultIcon = cat_icon_data[i]
                    break
                }
            }
            category.icon = defaultIcon
        }
        return categories
    }

    fun insertCategoryVersion3(db: SQLiteDatabase, categories: List<Category>) {
        var values: ContentValues
        try {
            for (category in categories) {
                values = ContentValues()
                values.put(COL_C_ID, category.id)
                values.put(COL_C_NAME, category.name)
                values.put(COL_C_COLOR, category.color)
                values.put(COL_C_ICON, category.icon)
                db.insert(TABLE_CATEGORY, null, values) // Inserting Row
            }
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        }

    }

    fun insertNoteVerion3(db: SQLiteDatabase, notes: List<Note>) {
        var values: ContentValues? = null
        try {
            for (note in notes) {
                values = ContentValues()
                values.put(COL_N_ID, note.id)
                values.put(COL_N_TITLE, note.tittle)
                values.put(COL_N_CONTENT, note.content)
                values.put(COL_N_FAV, note.favourite)
                values.put(COL_N_LAST_EDIT, note.lastEdit)
                values.put(COL_N_CATEGORY, note.category!!.id)
                db.insert(TABLE_NOTE, null, values)
            }
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        }

    }

    private fun createTableCategoryIconVersion3(db: SQLiteDatabase) {
        val CREATE_TABLE = """CREATE TABLE $TABLE_CATEGORY_ICON ("
        +COL_C_ID + " INTEGER PRIMARY KEY , "
        +COL_C_ICON + " TEXT, "
        +COL_C_COLOR + " TEXT "
        +" )"""
        try {
            db.execSQL(CREATE_TABLE)
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        }

    }

    private fun createTableCategoryVersion3(db: SQLiteDatabase) {
        val CREATE_TABLE = """CREATE TABLE $TABLE_CATEGORY ("
        +COL_C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        +COL_C_NAME + " TEXT, "
        +COL_C_COLOR + " TEXT, "
        +COL_C_ICON + " TEXT "
        +" )"""
        try {
            db.execSQL(CREATE_TABLE)
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        }

    }

    private fun createTableNoteVersion3(db: SQLiteDatabase) {
        val CREATE_TABLE = """CREATE TABLE $TABLE_NOTE ("
        +COL_N_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        +COL_N_TITLE + " TEXT, "
        +COL_N_CONTENT + " TEXT, "
        +COL_N_FAV + " INTEGER, "
        +COL_N_LAST_EDIT + " NUMERIC, "
        +COL_N_CATEGORY + " INTEGER, "
        +" FOREIGN KEY(" + COL_N_CATEGORY + ") REFERENCES " + TABLE_NOTE + "(" + COL_C_ID + ")"
        +")"""
        try {
            db.execSQL(CREATE_TABLE)
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        }

    }

    fun defineCategoryIconVersion3(db: SQLiteDatabase) {
        try {
            for (i in cat_icon_data.indices) {
                val values = ContentValues()
                values.put(COL_C_ICON, cat_icon_data[i])
                values.put(COL_C_COLOR, cat_color_data[i])
                Log.e("ICON DATA: ", i.toString() + " | " + cat_icon_data[i])
                db.insert(TABLE_CATEGORY_ICON, null, values) // Inserting Row
            }
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        }

    }

    companion object {

        private val DB_NAME = "noolis.db"

        private val TABLE_NOTE = "note"
        private val TABLE_CATEGORY = "category"
        private val TABLE_CATEGORY_ICON = "category_icon"

        private val COL_N_ID = "n_id"
        private val COL_N_TITLE = "n_title"
        private val COL_N_CONTENT = "n_content"
        private val COL_N_FAV = "n_favourite"
        private val COL_N_LAST_EDIT = "n_last_edit"
        private val COL_N_CATEGORY = "n_category"

        private val COL_C_ID = "c_id"
        private val COL_C_NAME = "c_name"
        private val COL_C_COLOR = "c_color"
        private val COL_C_ICON = "c_icon"

        private val DB_VERSION = 2
    }
}
