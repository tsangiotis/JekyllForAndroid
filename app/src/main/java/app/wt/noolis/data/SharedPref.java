package app.wt.noolis.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import app.wt.noolis.R;

public class SharedPref {
    private Context context;
    private SharedPreferences sharedPreferences;

    public static final String FIRST_LAUNCH_KEY = "app.wt.noolis.data.FIRST_LAUNCH_KEY";
    public static final String USER_NAME_KEY = "app.wt.noolis.data.USER_NAME_KEY";
    public static final String NAME_EDIT_KEY = "app.wt.noolis.data.NAME_EDIT_KEY";

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("MAIN_PREF", Context.MODE_PRIVATE);
    }


    public void setFirstLaunch(boolean flag) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(FIRST_LAUNCH_KEY, flag);
        editor.commit();
    }

    public boolean isFirstLaunch() {
        return sharedPreferences.getBoolean(FIRST_LAUNCH_KEY, true);
    }

    public void setUserName(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_NAME_KEY, name);
        editor.commit();
    }

    public void clearUserName(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(USER_NAME_KEY);
        editor.commit();
    }

    public String getUserName() {
        return sharedPreferences.getString(USER_NAME_KEY, getRes().getString(R.string.str_user_name));
    }

    public void setNameNeverEdit(boolean flag) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(NAME_EDIT_KEY, flag);
        editor.commit();
    }

    public boolean isNameNeverEdit() {
        return sharedPreferences.getBoolean(NAME_EDIT_KEY, true);
    }

    private Resources getRes() {
        return context.getResources();
    }
}
