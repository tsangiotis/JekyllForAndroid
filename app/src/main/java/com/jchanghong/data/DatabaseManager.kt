package com.jchanghong.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.jchanghong.GlobalApplication
import com.jchanghong.R
import com.jchanghong.data.DatabaseManager.DB_NAME
import com.jchanghong.data.DatabaseManager.DB_VERSION
import com.jchanghong.model.Category
import com.jchanghong.model.CategoryIcon
import com.jchanghong.model.Note
import com.jchanghong.utils.Tools


object DatabaseManager : SQLiteOpenHelper(GlobalApplication.mcontext, DB_NAME, null, DB_VERSION) {
    private val context: Context=GlobalApplication.mcontext
    val LOG=DatabaseManager::class.java.name+"jiangchanghong"
     val cat_id: IntArray
     val cat_name: Array<String>
     val cat_color: Array<String>
     val cat_icon: Array<String>
     val cat_icon_data: Array<String>
     val cat_color_data: Array<String>
    val defaultCAT:Category
    init {
            cat_id = context.resources.getIntArray(R.array.category_id)
            cat_name = context.resources.getStringArray(R.array.category_name)
            cat_color = context.resources.getStringArray(R.array.category_color)
            cat_icon = context.resources.getStringArray(R.array.category_icon)
            cat_icon_data = context.resources.getStringArray(R.array.category_icon_data)
            cat_color_data = context.resources.getStringArray(R.array.category_color_data)
         defaultCAT=Category()

    }
    override fun onCreate(DatabaseManager: SQLiteDatabase) {
        createTableCategoryIcon(DatabaseManager)
        createTableCategory(DatabaseManager)
        createTableNote(DatabaseManager)
    }

    override fun onUpgrade(DatabaseManager: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion == 1 && newVersion == 2) {
            val categories = getAllCategoryVersion3(DatabaseManager)
            Log.e("onUpgrade", "MASUK 1 2")
            val notes = getAllNotesVersion3(DatabaseManager)

            deleteCategoryIconTable(DatabaseManager)
            deleteNoteTable(DatabaseManager)
            deleteCategoryTable(DatabaseManager)

            createTableCategoryIconVersion3(DatabaseManager)
            createTableCategoryVersion3(DatabaseManager)
            createTableNoteVersion3(DatabaseManager)

            defineCategoryIconVersion3(DatabaseManager)
            insertCategoryVersion3(DatabaseManager, redefineCategoryVersion3(categories))
            insertNoteVerion3(DatabaseManager, notes)
//            NoteCache.clear()
//            CategoryCache.clear()
        }

    }

    private fun createTableNote(DatabaseManager: SQLiteDatabase) {
        val CREATE_TABLE = "CREATE TABLE ${TABLE_NOTE} ("+
        COL_N_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+ COL_N_TITLE +
                " TEXT, "+ COL_N_CONTENT + " TEXT, "+ COL_N_FAV + " INTEGER, "+
                COL_N_LAST_EDIT + " NUMERIC, "+ COL_N_CATEGORY +
                " INTEGER, "+" FOREIGN KEY(" + COL_N_CATEGORY +
                ") REFERENCES " + TABLE_CATEGORY + "(" + COL_C_ID + ")"+")"
        try {
            DatabaseManager.execSQL(CREATE_TABLE)
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        }

    }

    private fun createTableCategory(DatabaseManager: SQLiteDatabase) {
        val CREATE_TABLE = "CREATE TABLE ${TABLE_CATEGORY} ("+
                COL_C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                COL_C_NAME + " TEXT, "+ COL_C_COLOR +
                " TEXT, "+ COL_C_ICON + " TEXT "+" )"
        try {
            DatabaseManager.execSQL(CREATE_TABLE)
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        }

    }

    fun log(s: String)  {
        Log.d(LOG,s)
    }
    fun defineCategory() {
        val DatabaseManager = this.writableDatabase
        for (i in cat_id.indices) {
            val values = ContentValues()
            values.put(COL_C_ID, cat_id[i])
            values.put(COL_C_NAME, cat_name[i])
            values.put(COL_C_COLOR, cat_color[i])
            values.put(COL_C_ICON, cat_icon[i])
          DatabaseManager.insert(TABLE_CATEGORY, null, values) // Inserting Row
            log("insert cat:${cat_name[i]}")
        }
        DatabaseManager.close()
    }

    private fun createTableCategoryIcon(DatabaseManager: SQLiteDatabase) {
        val CREATE_TABLE = "CREATE TABLE ${TABLE_CATEGORY_ICON} ("+
                COL_C_ID + " INTEGER PRIMARY KEY , "+
                COL_C_ICON + " TEXT, "+
                COL_C_COLOR + " TEXT "+" )"
        try {
            DatabaseManager.execSQL(CREATE_TABLE)
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        }

    }

    fun defineCategoryIcon() {
        val DatabaseManager = this.writableDatabase
        for (i in cat_icon_data.indices) {
            val values = ContentValues()
            values.put(COL_C_ICON, cat_icon_data[i])
            values.put(COL_C_COLOR, cat_color_data[i])
            DatabaseManager.insert(TABLE_CATEGORY_ICON, null, values) // Inserting Row
        }
        DatabaseManager.close()
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
        values.put(COL_N_CATEGORY, note.category.id)
        val DatabaseManager = this.writableDatabase
        try {
         note.id= DatabaseManager.insert(TABLE_NOTE, null, values)
            log("insertnote ${note.tittle}")
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        } finally {
            DatabaseManager.close()
        }

    }

//    @RequiresApi(Build.VERSION_CODES.N)
    fun deleteNote(rowId: Long) {
        val DatabaseManager = this.writableDatabase
        try {
            DatabaseManager.delete(TABLE_NOTE, "$COL_N_ID =  $rowId", null)
            log("delete note: ${rowId}")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Db Error", e.toString())
        } finally {
            DatabaseManager.close()
        }
    }

    fun updateNote(note: Note) {
        val contentValues = ContentValues()
        contentValues.put(COL_N_TITLE, note.tittle)
        contentValues.put(COL_N_CONTENT, note.content)
        contentValues.put(COL_N_LAST_EDIT, note.lastEdit)
        contentValues.put(COL_N_CATEGORY, note.category.id)
        val DatabaseManager = this.writableDatabase
        try {
            DatabaseManager.update(TABLE_NOTE, contentValues, "$COL_N_ID = ${note.id}", null)
            Log.i(LOG,"update note:${note.tittle}")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Db Error", e.toString())
        } finally {
            DatabaseManager.close()
        }
    }

    operator fun get(id: Long): Note? {
        val DatabaseManager = this.readableDatabase
        var note:Note
        var cur: Cursor?
        try {
            cur = DatabaseManager.rawQuery("SELECT * FROM ${TABLE_NOTE} WHERE ${COL_N_ID} = ?", arrayOf(id.toString()))
            if (cur?.moveToFirst() == true) {

                note = getNoteFromCursor(cur)
            }
            else{
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Db Error", e.toString())
            return null
        } finally {
            DatabaseManager.close()
        }
        return note
    }

    val allNotes: List<Note>
        get() {
            val notes = ArrayList<Note>()
            val DatabaseManager = this.readableDatabase
            var cur: Cursor? = null
            try {
                cur = DatabaseManager.rawQuery("SELECT * FROM ${TABLE_NOTE} ORDER BY ${COL_N_ID} DESC", null)
                if (cur?.moveToFirst()==true) {
                    if (!cur.isAfterLast) {
                        do {
                            notes.add(getNoteFromCursor(cur))
                        } while (cur.moveToNext())
                    }
                }
            return notes
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("DB ERROR", e.toString())
                return notes
            } finally {
                cur?.close()
                DatabaseManager.close()
            }
//            NoteCache.addAll(notes)
            return notes
        }

    fun getNotesByCategoryId(cat_id: Long): List<Note> {
        val notes = ArrayList<Note>()
        var cur: Cursor? = null
        val DatabaseManager = this.readableDatabase
        try {
            cur = DatabaseManager.rawQuery("SELECT * FROM ${TABLE_NOTE} WHERE ${COL_N_CATEGORY} = ?", arrayOf(cat_id.toString()))
            if (cur?.moveToFirst()==true) {
                if (!cur.isAfterLast) {
                    do {
                        notes.add(getNoteFromCursor(cur))
                    } while (cur.moveToNext())
                }
            }
            else{
                return notes
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("DB ERROR", e.toString())
            return notes
        } finally {
            cur!!.close()
            DatabaseManager.close()
        }
        return notes
    }

    fun getNotesCountByCategoryId(cat_id: Long): Int {
        var returnValue = 0
        var cursor: Cursor? = null
        val DatabaseManager = this.readableDatabase
        try {
            cursor = DatabaseManager.rawQuery("SELECT COUNT($COL_N_ID) FROM $TABLE_NOTE WHERE $COL_N_CATEGORY = ?", arrayOf(cat_id.toString()))
            if (cursor?.moveToFirst()==true) {
                returnValue=cursor.getInt(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("DB ERROR", e.toString())
            return returnValue
        } finally {
            cursor?.close()
            DatabaseManager.close()
        }
        return returnValue
    }

    private fun getNoteFromCursor(cur: Cursor): Note {
        val n = Note()
        n.id=(cur.getLong(0))
        n.tittle=(cur.getString(1))
        n.content=(cur.getString(2))
        n.favourite=(cur.getInt(3))
        n.lastEdit=(cur.getLong(4))
        n.category=getCategoryById(cur.getLong(5))?: defaultCAT
        return n
    }

    /**
     * All Category transaction
     */

    fun getCategoryById(id: Long): Category? {
        var category:Category?
        var cur: Cursor? = null
        val DatabaseManager = this.readableDatabase
        try {
            cur = DatabaseManager.rawQuery("SELECT * FROM ${TABLE_CATEGORY} WHERE ${COL_C_ID} = ?", arrayOf(id.toString()))
            if (cur?.moveToFirst() == true) {
                category = getCategoryByCursor(cur)
                return category
            }
        return null
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Db Error", e.toString())
            return null
        } finally {
            cur?.close()
            DatabaseManager.close()
        }
        return category
    }

    val allCategory: List<Category>
        get() {
            val categories = ArrayList<Category>()
            var cur: Cursor? = null
            val DatabaseManager = this.readableDatabase
            try {
                cur = DatabaseManager.rawQuery("SELECT * FROM " + TABLE_CATEGORY, null)
                if (cur?.moveToFirst() == true) {
                    if (!cur.isAfterLast) {
                        do {
                            categories.add(getCategoryByCursor(cur))
                        } while (cur.moveToNext())
                    }
                }
                return categories
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Db Error", e.toString())
            } finally {
                cur?.close()
                DatabaseManager.close()
            }
            return categories
        }

    private fun getCategoryByCursor(cur: Cursor): Category {
        val c = Category()
        c.id=(cur.getLong(0))
        c.name=(cur.getString(1))
        c.color=(cur.getString(2))
        c.icon=(cur.getString(3))
        c.note_count=(getNotesCountByCategoryId(c.id))
        return c
    }

    val categoryIcon: List<CategoryIcon>
        get() {
            val categoryIcons = ArrayList<CategoryIcon>()
            var cur: Cursor? = null
            val DatabaseManager = this.readableDatabase
            try {
                cur = DatabaseManager.rawQuery("SELECT * FROM " + TABLE_CATEGORY_ICON, null)
                if (cur?.moveToFirst() == true) {
                    if (!cur.isAfterLast) {
                        do {
                            categoryIcons.add(getCategoryIconByCursor(cur))
                        } while (cur.moveToNext())
                    }
                }
                return categoryIcon

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Db Error", e.toString())
            } finally {
                cur!!.close()
                DatabaseManager.close()
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
        val DatabaseManager = this.writableDatabase
        try {
         category.id= DatabaseManager.insert(TABLE_CATEGORY, null, values) // Inserting Row
            Log.i(LOG,"insert cate ${category.name}")
//            CategoryCache.add(category)
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        } finally {
            DatabaseManager.close()
        }
    }

    fun updateCategory(category: Category) {
        val contentValues = ContentValues()
        contentValues.put(COL_C_ICON, category.icon)
        contentValues.put(COL_C_COLOR, category.color)
        contentValues.put(COL_C_NAME, category.name)
        val DatabaseManager = this.writableDatabase
        try {
            DatabaseManager.update(TABLE_CATEGORY, contentValues, "$COL_C_ID =${category.id}", null)
            Log.i(LOG,"update ca:$category")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Db Error", e.toString())
        } finally {
            DatabaseManager.close()
        }
    }

    fun deleteCategory(rowId: Long) {
        val DatabaseManager = this.writableDatabase
        try {
            DatabaseManager.delete(TABLE_CATEGORY, "$COL_C_ID=$rowId", null)
            Log.i(LOG,"delete ca $rowId")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Db Error", e.toString())
        } finally {
            DatabaseManager.close()
        }
    }

    val firstCategory: Category
        get() {
            var category = Category()
            var cur: Cursor? = null
            val DatabaseManager = this.readableDatabase
            try {
                cur = DatabaseManager.rawQuery("SELECT * FROM ${TABLE_CATEGORY} ORDER BY ROWID ASC LIMIT 1 ", null)
                if (cur?.moveToFirst() == true) {
                    category = getCategoryByCursor(cur)
                    return category
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Db Error", e.toString())
            } finally {
                cur?.close()
                DatabaseManager.close()
            }
            return defaultCAT
        }


    /**
     * All Favorites Note transaction
     */
    val allFavNote: List<Note>
        get() {
            val notes = ArrayList<Note>()
            var cur: Cursor? = null
            val DatabaseManager = this.readableDatabase
            try {
                cur = DatabaseManager.rawQuery("SELECT * FROM $TABLE_NOTE WHERE $COL_N_FAV = ? ORDER BY $COL_N_LAST_EDIT DESC", arrayOf("1"))

                if (cur?.moveToFirst() == true) {
                    if (!cur.isAfterLast) {
                        do {
                            notes.add(getNoteFromCursor(cur))
                        } while (cur.moveToNext())
                    }
                }
               return notes
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("DB ERROR", e.toString())
                return notes
            } finally {
                cur!!.close()
                DatabaseManager.close()
            }
            return notes
        }


    fun setFav(id: Long) {
        val contentValues = ContentValues()
        contentValues.put(COL_N_FAV, 1)
        val DatabaseManager = this.writableDatabase
        try {
            DatabaseManager.update(TABLE_NOTE, contentValues, "$COL_N_ID = $id", null)
            log("set fav for: $id")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Db Error", e.toString())
        } finally {
            DatabaseManager.close()
        }
    }

    fun removeFav(id: Long) {
        if (isFavoriteExist(id)) {
            val contentValues = ContentValues()
            contentValues.put(COL_N_FAV, 0)
            val DatabaseManager = this.writableDatabase
            try {
                DatabaseManager.update(TABLE_NOTE, contentValues, COL_N_ID + "=" + id, null)
                log("remove fav for id:$id")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Db Error", e.toString())
            } finally {
                DatabaseManager.close()
            }
        }
    }


    /**
     * Support method
     */
    private fun isFavoriteExist(id: Long): Boolean {
        var cursor: Cursor? = null
        val DatabaseManager = this.readableDatabase
        try {
            cursor = DatabaseManager.rawQuery("SELECT * FROM $TABLE_NOTE WHERE $COL_N_ID = ?", arrayOf(id.toString()))
            if (cursor?.moveToFirst() == true) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Db Error", e.toString())
        } finally {
            cursor?.close()
            DatabaseManager.close()
        }
        return false
    }

    val categorySize: Int
        get() {
            var returnVal = 0
            val DatabaseManager = this.readableDatabase
            var cursor: Cursor? = null
            try {
                cursor = DatabaseManager.rawQuery("SELECT COUNT(${COL_C_ID}) FROM ${TABLE_CATEGORY}", null)
                if (cursor?.moveToFirst() == true) {
                    returnVal = cursor.getInt(0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Db Error", e.toString())
            } finally {
                cursor!!.close()
                DatabaseManager.close()
            }
            return returnVal
        }

    /**
     * This is only for update 3.0
     */

    fun getAllCategoryVersion3(DatabaseManager: SQLiteDatabase): List<Category> {
        val categories = ArrayList<Category>()
        var cur: Cursor? = null
        try {
            cur = DatabaseManager.rawQuery("SELECT * FROM " + TABLE_CATEGORY, null)
            cur!!.moveToFirst()
            if (!cur.isAfterLast) {
                do {
                    categories.add(getCategoryByCursorVersion3(DatabaseManager, cur))
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

    private fun getCategoryByCursorVersion3(DatabaseManager: SQLiteDatabase, cur: Cursor): Category {
        val c = Category()
        c.id=(cur.getLong(0))
        c.name=(cur.getString(1))
        c.color=(cur.getString(2))
        c.icon=(cur.getString(3))
        c.note_count=(getNotesCountByCategoryIdVersion3(DatabaseManager, c.id))
        return c
    }

    fun getNotesCountByCategoryIdVersion3(DatabaseManager: SQLiteDatabase, cat_id: Long?): Int {
        var returnValue = 0
        var cursor: Cursor? = null
        try {
            cursor = DatabaseManager.rawQuery("SELECT COUNT(${COL_N_ID}) FROM ${TABLE_NOTE} WHERE ${COL_N_CATEGORY} = ?", arrayOf(cat_id!!.toString() + ""))
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

    fun getAllNotesVersion3(DatabaseManager: SQLiteDatabase): List<Note> {
        val notes = ArrayList<Note>()
        var cur: Cursor? = null
        try {
            cur = DatabaseManager.rawQuery("SELECT * FROM ${TABLE_NOTE} ORDER BY ${COL_N_ID} DESC", null)
            cur!!.moveToFirst()
            if (!cur.isAfterLast) {
                do {
                    notes.add(getNoteFromCursorVersion3(DatabaseManager, cur))
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

    private fun getNoteFromCursorVersion3(DatabaseManager: SQLiteDatabase, cur: Cursor): Note {
        val n = Note()
        n.id=(cur.getLong(0))
        n.tittle=(cur.getString(1))
        n.content=(cur.getString(2))
        n.favourite=(cur.getInt(3))
        n.lastEdit=(cur.getLong(4))
        n.category=(Category(cur.getLong(5), "", "", ""))
        return n
    }

    private fun deleteNoteTable(DatabaseManager: SQLiteDatabase) {
        DatabaseManager.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTE)
    }

    private fun deleteCategoryTable(DatabaseManager: SQLiteDatabase) {
        DatabaseManager.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY)
    }

    private fun deleteCategoryIconTable(DatabaseManager: SQLiteDatabase) {
        DatabaseManager.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY_ICON)
    }

    private fun redefineCategoryVersion3(categories: List<Category>): List<Category> {
        //this method is for check is the icon available or not,if not the icon will be replaced with default icon
        var defaultIcon = cat_icon_data[0]
        var ic = ""
        for (category in categories) {
            for (i in cat_icon_data.indices) {
                ic = Tools.StringToResId(cat_icon_data[i], context!!).toString()
                if (ic == category.icon) {
                    defaultIcon = cat_icon_data[i]
                    break
                }
            }
            category.icon=(defaultIcon)
        }
        return categories
    }

    fun insertCategoryVersion3(DatabaseManager: SQLiteDatabase, categories: List<Category>) {
        var values: ContentValues
        try {
            for (category in categories) {
                values = ContentValues()
                values.put(COL_C_ID, category.id)
                values.put(COL_C_NAME, category.name)
                values.put(COL_C_COLOR, category.color)
                values.put(COL_C_ICON, category.icon)
                DatabaseManager.insert(TABLE_CATEGORY, null, values) // Inserting Row
            }
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        }

    }

    fun insertNoteVerion3(DatabaseManager: SQLiteDatabase, notes: List<Note>) {
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
                DatabaseManager.insert(TABLE_NOTE, null, values)
            }
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        }

    }

    private fun createTableCategoryIconVersion3(DatabaseManager: SQLiteDatabase) {
        val CREATE_TABLE = "CREATE TABLE ${TABLE_CATEGORY_ICON} ("+
                COL_C_ID + " INTEGER PRIMARY KEY , "+
                COL_C_ICON + " TEXT, "+ COL_C_COLOR + " TEXT "+" )"
        try {
            DatabaseManager.execSQL(CREATE_TABLE)
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        }

    }

    private fun createTableCategoryVersion3(DatabaseManager: SQLiteDatabase) {
        val CREATE_TABLE = "CREATE TABLE ${TABLE_CATEGORY} ("+
                COL_C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                COL_C_NAME + " TEXT, "+ COL_C_COLOR + " TEXT, "+ COL_C_ICON + " TEXT "+" )"
        try {
            DatabaseManager.execSQL(CREATE_TABLE)
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        }

    }

    private fun createTableNoteVersion3(DatabaseManager: SQLiteDatabase) {
        val CREATE_TABLE = "CREATE TABLE ${TABLE_NOTE} ("+
                COL_N_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+ COL_N_TITLE + " TEXT, "+ COL_N_CONTENT + " TEXT, "+ COL_N_FAV + " INTEGER, "+ COL_N_LAST_EDIT + " NUMERIC, "+ COL_N_CATEGORY + " INTEGER, "+" FOREIGN KEY(" + COL_N_CATEGORY + ") REFERENCES " + TABLE_NOTE + "(" + COL_C_ID + ")"+")"
        try {
            DatabaseManager.execSQL(CREATE_TABLE)
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        }

    }

    fun defineCategoryIconVersion3(DatabaseManager: SQLiteDatabase) {
        try {
            for (i in cat_icon_data.indices) {
                val values = ContentValues()
                values.put(COL_C_ICON, cat_icon_data[i])
                values.put(COL_C_COLOR, cat_color_data[i])
                Log.e("ICON DATA: ", i.toString() + " | " + cat_icon_data[i])
                DatabaseManager.insert(TABLE_CATEGORY_ICON, null, values) // Inserting Row
            }
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        }

    }


     const   internal val DB_NAME = "noolis.DatabaseManager"

        const   internal val TABLE_NOTE = "note"
        const     internal val TABLE_CATEGORY = "category"
        const    internal val TABLE_CATEGORY_ICON = "category_icon"

        const   internal val COL_N_ID = "n_id"
        const   internal val COL_N_TITLE = "n_title"
        const    internal val COL_N_CONTENT = "n_content"
        const    internal val COL_N_FAV = "n_favourite"
        const    internal val COL_N_LAST_EDIT = "n_last_edit"
        const    internal val COL_N_CATEGORY = "n_category"

        const   internal val COL_C_ID = "c_id"
           const   internal val COL_C_NAME = "c_name"
        const    internal val COL_C_COLOR = "c_color"
        const   internal val COL_C_ICON = "c_icon"

        const    internal val DB_VERSION = 2

    fun insertNoteorupdate(note: Note) {
        var stemp = allNotes.find { it.tittle == note.tittle }
        if (stemp === null) {
            insertNote(note)
        }
        else{
            if (stemp.content != note.content) {
                note.id=stemp.id
                updateNote(note)
            }
        }
    }
}
