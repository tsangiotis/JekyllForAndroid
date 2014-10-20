package gr.tsagi.jekyllforandroid.app.activities;

import android.os.Build;
import android.os.Bundle;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import gr.tsagi.jekyllforandroid.app.R;
import gr.tsagi.jekyllforandroid.app.fragments.PrefsFragment;

/**
 * Created by tsagi on 9/9/13.
 */
public class SetPreferenceActivity extends BaseActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // Set color
            tintManager.setTintColor(getResources().getColor(R.color.primary));
        }

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commit();

    }

    @Override
    protected int getLayoutResource() {
        return 0;
    }
}