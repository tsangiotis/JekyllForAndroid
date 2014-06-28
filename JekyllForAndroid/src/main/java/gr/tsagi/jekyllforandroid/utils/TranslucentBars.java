package gr.tsagi.jekyllforandroid.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;

/**
 * Created by tsagi on 2/4/14.
 */

public class TranslucentBars{

    Activity context;

    public TranslucentBars(Activity act){
        this.context = act;
        tint(false);
    }

    public void tint(boolean on){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
        }

        SystemBarTintManager tintManager = new SystemBarTintManager(context);
        // enable status bar tint
//        tintManager.setStatusBarTintEnabled(on);
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = context.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

}
