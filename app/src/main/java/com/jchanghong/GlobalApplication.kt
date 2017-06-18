package com.jchanghong

import android.app.Application
import android.util.Log
import com.jchanghong.data.DatabaseManager
import com.jchanghong.data.SharedPref
import com.jchanghong.model.Note

class GlobalApplication : Application() {

   lateinit private var db: DatabaseManager
  lateinit  private var sharedPref: SharedPref

    override fun onCreate() {
        // init db
        db = DatabaseManager(this)
        sharedPref = SharedPref(applicationContext)

        //first launch
        if (sharedPref.isFirstLaunch) {

            //define category
            db.defineCategory()

            Log.e("onCreate", "GlobalApplication : defineCategory")

            //define category icon
            db.defineCategoryIcon()

            //sample data 1
            val sampleNote = Note()
            sampleNote.tittle = getString(R.string.dummy_title_1)
            sampleNote.content = getString(R.string.dummy_content_1)
            sampleNote.lastEdit = System.currentTimeMillis()
            sampleNote.category = db.getCategoryById(resources.getIntArray(R.array.category_id)[0].toLong())
            db.insertNote(sampleNote)

            //sample data 2
            val sampleNote2 = Note()
            sampleNote2.tittle = getString(R.string.dummy_title_2)
            sampleNote2.content = getString(R.string.dummy_content_2)
            sampleNote2.lastEdit = System.currentTimeMillis()
                sampleNote2.category = db.getCategoryById(resources.getIntArray(R.array.category_id)[1].toLong())
            db.insertNote(sampleNote2)

            //sample data 3
            val sampleNote3 = Note()
            sampleNote3.tittle = getString(R.string.dummy_title_3)
            sampleNote3.content = getString(R.string.dummy_content_3)
            sampleNote3.lastEdit = System.currentTimeMillis()
                sampleNote3.category = db.getCategoryById(resources.getIntArray(R.array.category_id)[2].toLong())
            db.insertNote(sampleNote3)

            sharedPref.isFirstLaunch = false
        }
        super.onCreate()

    }
}
