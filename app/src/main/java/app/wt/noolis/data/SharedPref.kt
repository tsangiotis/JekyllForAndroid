package app.wt.noolis.data

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import app.wt.noolis.R

class SharedPref(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MAIN_PREF", Context.MODE_PRIVATE)

    var isFirstLaunch: Boolean
        get() = sharedPreferences.getBoolean(FIRST_LAUNCH_KEY, true)
        set(flag) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(FIRST_LAUNCH_KEY, flag)
            editor.commit()
        }

    var userName: String
        get() = sharedPreferences.getString(USER_NAME_KEY, res.getString(R.string.str_user_name))
        set(name) {
            val editor = sharedPreferences.edit()
            editor.putString(USER_NAME_KEY, name)
            editor.commit()
        }

    var isNameNeverEdit: Boolean
        get() = sharedPreferences.getBoolean(NAME_EDIT_KEY, true)
        set(flag) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(NAME_EDIT_KEY, flag)
            editor.commit()
        }

    private val res: Resources
        get() = context.resources

    companion object {

        val FIRST_LAUNCH_KEY = "app.wt.noolis.data.FIRST_LAUNCH_KEY"
        val USER_NAME_KEY = "app.wt.noolis.data.USER_NAME_KEY"
        val NAME_EDIT_KEY = "app.wt.noolis.data.NAME_EDIT_KEY"
    }
}
