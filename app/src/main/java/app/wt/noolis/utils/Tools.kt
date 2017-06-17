package app.wt.noolis.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.WindowManager
import app.wt.noolis.R
import java.text.SimpleDateFormat
import java.util.*

/**
\* Created with IntelliJ IDEA.
\* User: muslim
\* Date: 25/11/2015
\* Time: 9:15
\*/
object Tools {
    private val apiVerison: Float
        get() {

            var f: Float? = null
            try {
                val strBuild = StringBuilder()
                strBuild.append(android.os.Build.VERSION.RELEASE.substring(0, 2))
                f = strBuild.toString().toFloat()
            } catch (e: NumberFormatException) {
                Log.e("", "Error when get API" + e.message)
            }

            return f!!.toFloat()
        }

    fun systemBarLolipop(act: Activity) {
        if (apiVerison >= 5.0) {
            val window = act.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = act.resources.getColor(R.color.colorPrimaryDark)
        }
    }

    fun systemBarLolipopCustom(act: Activity, color: String) {
        if (apiVerison >= 5.0) {
            val window = act.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = coloringDarker(color)
        }
    }

    fun coloringDarker(color: String): Int {
        val hsv = FloatArray(3)
        val c = Color.parseColor(color)
        Color.colorToHSV(c, hsv)
        hsv[2] *= 0.8f // value component
        return Color.HSVToColor(hsv)
    }

    fun stringToDate(`val`: Long): String {
        val date = Date(`val`)
        val df2 = SimpleDateFormat("MMM, dd yyyy")
        val dateText = df2.format(date)
        return dateText
    }

    val nowDate: String
        get() {
            val date = Date(System.currentTimeMillis())
            val df2 = SimpleDateFormat("MMMM, dd yyyy")
            val dateText = df2.format(date)
            return dateText
        }

    fun rateAction(activity: Activity) {
        val uri = Uri.parse("market://details?id=" + activity.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            activity.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + activity.packageName)))
        }

    }

    fun StringToResId(drw: String, context: Context): Int {
        val resourceId = context.resources.getIdentifier(drw, "drawable", context.packageName)
        return resourceId
    }

}
