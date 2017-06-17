package com.jchanghong.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import com.jchanghong.R;
import com.jchanghong.model.Category;
import com.jchanghong.model.CategoryIcon;
import com.jchanghong.model.Note;
import com.jchanghong.utils.Tools;

public class DatabaseManager extends SQLiteOpenHelper {

    private static final String DB_NAME = "noolis.db";

    private static final String TABLE_NOTE = "note";
    private static final String TABLE_CATEGORY = "category";
    private static final String TABLE_CATEGORY_ICON = "category_icon";

    private static final String COL_N_ID = "n_id";
    private static final String COL_N_TITLE = "n_title";
    private static final String COL_N_CONTENT = "n_content";
    private static final String COL_N_FAV = "n_favourite";
    private static final String COL_N_LAST_EDIT = "n_last_edit";
    private static final String COL_N_CATEGORY = "n_category";

    private static final String COL_C_ID = "c_id";
    private static final String COL_C_NAME = "c_name";
    private static final String COL_C_COLOR = "c_color";
    private static final String COL_C_ICON = "c_icon";

    private static final int DB_VERSION = 2;

    private final Context context;

    private int cat_id[];
    private String cat_name[];
    private String cat_color[];
    private String cat_icon[];
    private String cat_icon_data[];
    private String cat_color_data[];

    public DatabaseManager(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
        this.context = ctx;

        cat_id = ctx.getResources().getIntArray(R.array.category_id);
        cat_name = ctx.getResources().getStringArray(R.array.category_name);
        cat_color = ctx.getResources().getStringArray(R.array.category_color);
        cat_icon = ctx.getResources().getStringArray(R.array.category_icon);
        cat_icon_data = ctx.getResources().getStringArray(R.array.category_icon_data);
        cat_color_data = ctx.getResources().getStringArray(R.array.category_color_data);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTableNote(db);
        createTableCategory(db);
        createTableCategoryIcon(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            List<Category> categories = getAllCategoryVersion3(db);
            Log.e("onUpgrade", "MASUK 1 2");
            List<Note> notes = getAllNotesVersion3(db);

            deleteCategoryIconTable(db);
            deleteNoteTable(db);
            deleteCategoryTable(db);

            createTableCategoryIconVersion3(db);
            createTableCategoryVersion3(db);
            createTableNoteVersion3(db);

            defineCategoryIconVersion3(db);
            insertCategoryVersion3(db,redefineCategoryVersion3(categories));
            insertNoteVerion3(db,notes);
        }

    }

    private void createTableNote(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NOTE + " ("
                + COL_N_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_N_TITLE + " TEXT, "
                + COL_N_CONTENT + " TEXT, "
                + COL_N_FAV + " INTEGER, "
                + COL_N_LAST_EDIT + " NUMERIC, "
                + COL_N_CATEGORY + " INTEGER, "
                + " FOREIGN KEY(" + COL_N_CATEGORY + ") REFERENCES " + TABLE_NOTE + "(" + COL_C_ID + ")"
                + ")";
        try {
            db.execSQL(CREATE_TABLE);
        } catch (Exception e) {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
    }

    private void createTableCategory(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_CATEGORY + " ("
                + COL_C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_C_NAME + " TEXT, "
                + COL_C_COLOR + " TEXT, "
                + COL_C_ICON + " TEXT "
                + " )";
        try {
            db.execSQL(CREATE_TABLE);
        } catch (Exception e) {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
    }

    public void defineCategory() {
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 0; i < cat_id.length; i++) {
            ContentValues values = new ContentValues();
            //values.put(COL_C_ID, cat_id[i]);
            values.put(COL_C_NAME, cat_name[i]);
            values.put(COL_C_COLOR, cat_color[i]);
            values.put(COL_C_ICON, cat_icon[i]);
            Log.e("ICON : ", i + " | " + cat_icon[i]);
            db.insert(TABLE_CATEGORY, null, values); // Inserting Row
        }
        db.close();
    }

    private void createTableCategoryIcon(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_CATEGORY_ICON + " ("
                + COL_C_ID + " INTEGER PRIMARY KEY , "
                + COL_C_ICON + " TEXT, "
                + COL_C_COLOR + " TEXT "
                + " )";
        try {
            db.execSQL(CREATE_TABLE);
        } catch (Exception e) {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
    }

    public void defineCategoryIcon() {
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 0; i < cat_icon_data.length; i++) {
            ContentValues values = new ContentValues();
            values.put(COL_C_ICON, cat_icon_data[i]);
            values.put(COL_C_COLOR, cat_color_data[i]);
            Log.e("ICON DATA: ", i + " | " + cat_icon_data[i]);
            db.insert(TABLE_CATEGORY_ICON, null, values); // Inserting Row
        }
        db.close();
    }


    /**
     * All Note transaction
     */
    public void insertNote(Note note) {
        ContentValues values = new ContentValues();
        values.put(COL_N_TITLE, note.getTittle());
        values.put(COL_N_CONTENT, note.getContent());
        values.put(COL_N_FAV, note.getFavourite());
        values.put(COL_N_LAST_EDIT, note.getLastEdit());
        values.put(COL_N_CATEGORY, note.getCategory().getId());
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.insert(TABLE_NOTE, null, values);
        } catch (Exception e) {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public void deleteNote(long rowId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_NOTE, COL_N_ID + "=" + rowId, null);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Db Error", e.toString());
        } finally {
            db.close();
        }
    }

    public void updateNote(Note note) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_N_TITLE, note.getTittle());
        contentValues.put(COL_N_CONTENT, note.getContent());
        contentValues.put(COL_N_LAST_EDIT, note.getLastEdit());
        contentValues.put(COL_N_CATEGORY, note.getCategory().getId());
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.update(TABLE_NOTE, contentValues, COL_N_ID + "=" + note.getId(), null);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Db Error", e.toString());
        } finally {
            db.close();
        }
    }

    public Note get(Long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Note note = new Note();
        Cursor cur = null;
        try {
            cur = db.rawQuery("SELECT * FROM " + TABLE_NOTE + " WHERE " + COL_N_ID + " = ?", new String[]{id + ""});
            cur.moveToFirst();
            note = getNoteFromCursor(cur);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Db Error", e.toString());
        } finally {
            db.close();
        }
        return note;
    }

    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = null;
        try {
            cur = db.rawQuery("SELECT * FROM " + TABLE_NOTE + " ORDER BY " + COL_N_ID + " DESC", null);
            cur.moveToFirst();
            if (!cur.isAfterLast()) {
                do {
                    notes.add(getNoteFromCursor(cur));
                } while (cur.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DB ERROR", e.toString());
        } finally {
            cur.close();
            db.close();
        }
        return notes;
    }

    public List<Note> getNotesByCategoryId(Long cat_id) {
        List<Note> notes = new ArrayList<>();
        Cursor cur = null;
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            cur = db.rawQuery("SELECT * FROM " + TABLE_NOTE + " WHERE " + COL_N_CATEGORY + " = ?", new String[]{cat_id + ""});
            cur.moveToFirst();
            if (!cur.isAfterLast()) {
                do {
                    notes.add(getNoteFromCursor(cur));
                } while (cur.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DB ERROR", e.toString());
        } finally {
            cur.close();
            db.close();
        }
        return notes;
    }

    public int getNotesCountByCategoryId(Long cat_id) {
        int returnValue = 0;
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            cursor = db.rawQuery("SELECT COUNT(" + COL_N_ID + ") FROM " + TABLE_NOTE + " WHERE " + COL_N_CATEGORY + " = ?", new String[]{cat_id + ""});
            cursor.moveToFirst();
            returnValue = cursor.getInt(0);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DB ERROR", e.toString());
        } finally {
            cursor.close();
            db.close();
        }
        return returnValue;
    }

    private Note getNoteFromCursor(Cursor cur) {
        Note n = new Note();
        n.setId(cur.getLong(0));
        n.setTittle(cur.getString(1));
        n.setContent(cur.getString(2));
        n.setFavourite(cur.getInt(3));
        n.setLastEdit(cur.getLong(4));
        n.setCategory(getCategoryById(cur.getLong(5)));
        return n;
    }

    /**
     * All Category transaction
     */

    public Category getCategoryById(long id) {
        Category category = new Category();
        Cursor cur = null;
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            cur = db.rawQuery("SELECT * FROM " + TABLE_CATEGORY + " WHERE " + COL_C_ID + " = ?", new String[]{id + ""});
            cur.moveToFirst();
            category = getCategoryByCursor(cur);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Db Error", e.toString());
        } finally {
            cur.close();
            db.close();
        }
        return category;
    }

    public List<Category> getAllCategory() {
        List<Category> categories = new ArrayList<>();
        Cursor cur = null;
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            cur = db.rawQuery("SELECT * FROM " + TABLE_CATEGORY, null);
            cur.moveToFirst();
            if (!cur.isAfterLast()) {
                do {
                    categories.add(getCategoryByCursor(cur));
                } while (cur.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Db Error", e.toString());
        } finally {
            cur.close();
            db.close();
        }
        return categories;
    }

    private Category getCategoryByCursor(Cursor cur) {
        Category c = new Category();
        c.setId(cur.getLong(0));
        c.setName(cur.getString(1));
        c.setColor(cur.getString(2));
        c.setIcon(cur.getString(3));
        c.setNote_count(getNotesCountByCategoryId(c.getId()));
        return c;
    }

    public List<CategoryIcon> getCategoryIcon() {
        List<CategoryIcon> categoryIcons = new ArrayList<>();
        Cursor cur = null;
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            cur = db.rawQuery("SELECT * FROM " + TABLE_CATEGORY_ICON, null);
            cur.moveToFirst();
            if (!cur.isAfterLast()) {
                do {
                    categoryIcons.add(getCategoryIconByCursor(cur));
                } while (cur.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Db Error", e.toString());
        } finally {
            cur.close();
            db.close();
        }
        return categoryIcons;
    }

    private CategoryIcon getCategoryIconByCursor(Cursor cur) {
        CategoryIcon ci = new CategoryIcon();
        ci.setIcon(cur.getString(1));
        ci.setColor(cur.getString(2));
        return ci;
    }

    public void insertCategory(Category category) {
        ContentValues values = new ContentValues();
        values.put(COL_C_NAME, category.getName());
        values.put(COL_C_COLOR, category.getColor());
        values.put(COL_C_ICON, category.getIcon());
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.insert(TABLE_CATEGORY, null, values); // Inserting Row
        } catch (Exception e) {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public void updateCategory(Category category) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_C_ICON, category.getIcon());
        contentValues.put(COL_C_COLOR, category.getColor());
        contentValues.put(COL_C_NAME, category.getName());
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.update(TABLE_CATEGORY, contentValues, COL_C_ID + "=" + category.getId(), null);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Db Error", e.toString());
        } finally {
            db.close();
        }
    }

    public void deleteCategory(long rowId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_CATEGORY, COL_C_ID + "=" + rowId, null);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Db Error", e.toString());
        } finally {
            db.close();
        }
    }

    public Category getFirstCategory() {
        Category category = new Category();
        Cursor cur = null;
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            cur = db.rawQuery(new StringBuilder().append("SELECT * FROM ").append(TABLE_CATEGORY).append(" ORDER BY ROWID ASC LIMIT 1 ").toString(), null);
            cur.moveToFirst();
            category = getCategoryByCursor(cur);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Db Error", e.toString());
        } finally {
            cur.close();
            db.close();
        }
        return category;
    }


    /**
     * All Favorites Note transaction
     */
    public List<Note> getAllFavNote() {
        List<Note> notes = new ArrayList<>();
        Cursor cur = null;
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            cur = db.rawQuery("SELECT * FROM " + TABLE_NOTE + " WHERE " + COL_N_FAV + " = ?" + " ORDER BY " + COL_N_ID + " DESC", new String[]{"1"});
            cur.moveToFirst();
            if (!cur.isAfterLast()) {
                do {
                    notes.add(getNoteFromCursor(cur));
                } while (cur.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DB ERROR", e.toString());
        } finally {
            cur.close();
            db.close();
        }
        return notes;
    }


    public void setFav(long id) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_N_FAV, 1);
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.update(TABLE_NOTE, contentValues, COL_N_ID + "=" + id, null);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Db Error", e.toString());
        } finally {
            db.close();
        }
    }

    public void removeFav(long id) {
        if (isFavoriteExist(id)) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_N_FAV, 0);
            SQLiteDatabase db = this.getWritableDatabase();
            try {
                db.update(TABLE_NOTE, contentValues, COL_N_ID + "=" + id, null);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Db Error", e.toString());
            } finally {
                db.close();
            }
        }
    }


    /**
     * Support method
     */
    private boolean isFavoriteExist(long id) {
        Cursor cursor = null;
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_NOTE + " WHERE " + COL_N_ID + " = ?", new String[]{id + ""});
            count = cursor.getCount();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Db Error", e.toString());
        } finally {
            cursor.close();
            db.close();
        }
        return (count > 0);
    }

    public int getCategorySize() {
        int returnVal = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(" + COL_C_ID + ") FROM " + TABLE_CATEGORY, null);
            cursor.moveToFirst();
            returnVal = cursor.getInt(0);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Db Error", e.toString());
        } finally {
            cursor.close();
            db.close();
        }
        return returnVal;
    }

    /**
     * This is only for update 3.0
     */

    public List<Category> getAllCategoryVersion3(SQLiteDatabase db) {
        List<Category> categories = new ArrayList<>();
        Cursor cur = null;
        try {
            cur = db.rawQuery("SELECT * FROM " + TABLE_CATEGORY, null);
            cur.moveToFirst();
            if (!cur.isAfterLast()) {
                do {
                    categories.add(getCategoryByCursorVersion3(db,cur));
                } while (cur.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Db Error", e.toString());
        } finally {
            cur.close();
        }
        return categories;
    }

    private Category getCategoryByCursorVersion3(SQLiteDatabase db,Cursor cur) {
        Category c = new Category();
        c.setId(cur.getLong(0));
        c.setName(cur.getString(1));
        c.setColor(cur.getString(2));
        c.setIcon(cur.getString(3));
        c.setNote_count(getNotesCountByCategoryIdVersion3(db,c.getId()));
        return c;
    }

    public int getNotesCountByCategoryIdVersion3(SQLiteDatabase db,Long cat_id) {
        int returnValue = 0;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(" + COL_N_ID + ") FROM " + TABLE_NOTE + " WHERE " + COL_N_CATEGORY + " = ?", new String[]{cat_id + ""});
            cursor.moveToFirst();
            returnValue = cursor.getInt(0);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DB ERROR", e.toString());
        } finally {
            cursor.close();
        }
        return returnValue;
    }

    public List<Note> getAllNotesVersion3(SQLiteDatabase db) {
        List<Note> notes = new ArrayList<>();
        Cursor cur = null;
        try {
            cur = db.rawQuery("SELECT * FROM " + TABLE_NOTE + " ORDER BY " + COL_N_ID + " DESC", null);
            cur.moveToFirst();
            if (!cur.isAfterLast()) {
                do {
                    notes.add(getNoteFromCursorVersion3(db,cur));
                } while (cur.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DB ERROR", e.toString());
        } finally {
            cur.close();
        }
        return notes;
    }

    private Note getNoteFromCursorVersion3(SQLiteDatabase db,Cursor cur) {
        Note n = new Note();
        n.setId(cur.getLong(0));
        n.setTittle(cur.getString(1));
        n.setContent(cur.getString(2));
        n.setFavourite(cur.getInt(3));
        n.setLastEdit(cur.getLong(4));
        n.setCategory(new Category(cur.getLong(5),"","",""));
        return n;
    }

    private void deleteNoteTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTE);
    }

    private void deleteCategoryTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
    }

    private void deleteCategoryIconTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY_ICON);
    }

    private List<Category> redefineCategoryVersion3(List<Category> categories) {
        //this method is for check is the icon available or not,if not the icon will be replaced with default icon
        String defaultIcon = cat_icon_data[0];
        String ic = "";
        for (Category category : categories) {
            for (int i = 0; i < cat_icon_data.length; i++) {
                ic = String.valueOf(Tools.StringToResId(cat_icon_data[i], context));
                if (ic.equals(category.getIcon())) {
                    defaultIcon = cat_icon_data[i];
                    break;
                }
            }
            category.setIcon(defaultIcon);
        }
        return categories;
    }

    public void insertCategoryVersion3(SQLiteDatabase db,List<Category> categories) {
        ContentValues values;
        try {
            for (Category category : categories) {
                values = new ContentValues();
                values.put(COL_C_ID, category.getId());
                values.put(COL_C_NAME, category.getName());
                values.put(COL_C_COLOR, category.getColor());
                values.put(COL_C_ICON, category.getIcon());
                db.insert(TABLE_CATEGORY, null, values); // Inserting Row
            }
        } catch (Exception e) {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
    }

    public void insertNoteVerion3(SQLiteDatabase db,List<Note> notes) {
        ContentValues values = null;
        try {
            for (Note note : notes) {
                values = new ContentValues();
                values.put(COL_N_ID, note.getId());
                values.put(COL_N_TITLE, note.getTittle());
                values.put(COL_N_CONTENT, note.getContent());
                values.put(COL_N_FAV, note.getFavourite());
                values.put(COL_N_LAST_EDIT, note.getLastEdit());
                values.put(COL_N_CATEGORY, note.getCategory().getId());
                db.insert(TABLE_NOTE, null, values);
            }
        } catch (Exception e) {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
    }

    private void createTableCategoryIconVersion3(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_CATEGORY_ICON + " ("
                + COL_C_ID + " INTEGER PRIMARY KEY , "
                + COL_C_ICON + " TEXT, "
                + COL_C_COLOR + " TEXT "
                + " )";
        try {
            db.execSQL(CREATE_TABLE);
        } catch (Exception e) {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
    }

    private void createTableCategoryVersion3(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_CATEGORY + " ("
                + COL_C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_C_NAME + " TEXT, "
                + COL_C_COLOR + " TEXT, "
                + COL_C_ICON + " TEXT "
                + " )";
        try {
            db.execSQL(CREATE_TABLE);
        } catch (Exception e) {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
    }

    private void createTableNoteVersion3(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NOTE + " ("
                + COL_N_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_N_TITLE + " TEXT, "
                + COL_N_CONTENT + " TEXT, "
                + COL_N_FAV + " INTEGER, "
                + COL_N_LAST_EDIT + " NUMERIC, "
                + COL_N_CATEGORY + " INTEGER, "
                + " FOREIGN KEY(" + COL_N_CATEGORY + ") REFERENCES " + TABLE_NOTE + "(" + COL_C_ID + ")"
                + ")";
        try {
            db.execSQL(CREATE_TABLE);
        } catch (Exception e) {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
    }

    public void defineCategoryIconVersion3(SQLiteDatabase db) {
        try {
            for (int i = 0; i < cat_icon_data.length; i++) {
                ContentValues values = new ContentValues();
                values.put(COL_C_ICON, cat_icon_data[i]);
                values.put(COL_C_COLOR, cat_color_data[i]);
                Log.e("ICON DATA: ", i + " | " + cat_icon_data[i]);
                db.insert(TABLE_CATEGORY_ICON, null, values); // Inserting Row
            }
        } catch (Exception e) {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
    }
}
