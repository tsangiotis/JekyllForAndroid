package app.wt.noolis;

import android.app.Application;
import android.util.Log;

import app.wt.noolis.data.DatabaseManager;
import app.wt.noolis.data.SharedPref;
import app.wt.noolis.model.Note;
import app.wt.noolis.utils.Tools;

public class GlobalApplication extends Application {

    private DatabaseManager db;
    private SharedPref sharedPref;

    @Override
    public void onCreate() {
        // init db
        db = new DatabaseManager(this);
        sharedPref = new SharedPref(getApplicationContext());

        //first launch
        if (sharedPref.isFirstLaunch()) {

            //define category
            db.defineCategory();

            Log.e("onCreate", "GlobalApplication : defineCategory");

            //define category icon
            db.defineCategoryIcon();

            //sample data 1
            Note sampleNote = new Note();
            sampleNote.setTittle(getString(R.string.dummy_title_1));
            sampleNote.setContent(getString(R.string.dummy_content_1));
            sampleNote.setLastEdit(System.currentTimeMillis());
            sampleNote.setCategory(db.getCategoryById(getResources().getIntArray(R.array.category_id)[0]));
            db.insertNote(sampleNote);

            //sample data 2
            Note sampleNote2 = new Note();
            sampleNote2.setTittle(getString(R.string.dummy_title_2));
            sampleNote2.setContent(getString(R.string.dummy_content_2));
            sampleNote2.setLastEdit(System.currentTimeMillis());
            sampleNote2.setCategory(db.getCategoryById(getResources().getIntArray(R.array.category_id)[1]));
            db.insertNote(sampleNote2);

            //sample data 3
            Note sampleNote3 = new Note();
            sampleNote3.setTittle(getString(R.string.dummy_title_3));
            sampleNote3.setContent(getString(R.string.dummy_content_3));
            sampleNote3.setLastEdit(System.currentTimeMillis());
            sampleNote3.setCategory(db.getCategoryById(getResources().getIntArray(R.array.category_id)[2]));
            db.insertNote(sampleNote3);

            sharedPref.setFirstLaunch(false);
        }
        super.onCreate();

    }
}
