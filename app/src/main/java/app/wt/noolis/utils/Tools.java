package app.wt.noolis.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import app.wt.noolis.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by muslim on 25/11/2015.
 */
public class Tools {
    private static float getAPIVerison() {

        Float f = null;
        try {
            StringBuilder strBuild = new StringBuilder();
            strBuild.append(android.os.Build.VERSION.RELEASE.substring(0, 2));
            f = new Float(strBuild.toString());
        } catch (NumberFormatException e) {
            Log.e("", "Error when get API" + e.getMessage());
        }

        return f.floatValue();
    }

    public static void systemBarLolipop(Activity act){
        if (getAPIVerison() >= 5.0) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(act.getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    public static void systemBarLolipopCustom(Activity act, String color){
        if (getAPIVerison() >= 5.0) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(coloringDarker(color));
        }
    }

    public static int coloringDarker(String color){
        float[] hsv = new float[3];
        int c = Color.parseColor(color);
        Color.colorToHSV(c, hsv);
        hsv[2] *= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }

    public static String stringToDate(long val){
        Date date=new Date(val);
        SimpleDateFormat df2=new SimpleDateFormat("MMM, dd yyyy");
        String dateText=df2.format(date);
        return dateText;
    }
    public static String longToTime(Long val){
        Date date=new Date(val);
        SimpleDateFormat df2=new SimpleDateFormat("hh:ss aa");
        String dateText=df2.format(date);
        return dateText;
    }

    public static String getNowDate(){
        Date date=new Date(System.currentTimeMillis());
        SimpleDateFormat df2=new SimpleDateFormat("MMMM, dd yyyy");
        String dateText=df2.format(date);
        return dateText;
    }

    public static void rateAction(Activity activity){
        Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            activity.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + activity.getPackageName())));
        }
    }

    public static int StringToResId(String drw, Context context){
        int resourceId = context.getResources().getIdentifier(drw, "drawable", context.getPackageName());
        return resourceId;
    }

}
