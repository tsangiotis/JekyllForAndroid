package com.jchanghong;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.jchanghong.data.DatabaseManager;
import com.jchanghong.data.SharedPref;
import com.jchanghong.fragment.FragmentAbout;
import com.jchanghong.fragment.FragmentCategory;
import com.jchanghong.fragment.FragmentFavorites;
import com.jchanghong.fragment.FragmentNote;
import com.jchanghong.utils.Tools;

import com.jchanghong.R;

public class ActivityMain extends AppCompatActivity {

    //for ads

    private Toolbar toolbar;
    public ActionBar actionBar;
    private NavigationView navigationView;
    private FloatingActionButton floatingActionButton;
    private View parent_view;
    private TextView user_name;

    private DatabaseManager db;
    private SharedPref sharedPref;

    private int navigation = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        parent_view = findViewById(android.R.id.content);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);

        sharedPref = new SharedPref(this);
        db = new DatabaseManager(this); // init db

        prepareAds();
        initToolbar();
        initDrawerMenu();
        // cek cek

        // set home view
        actionBar.setTitle(getString(R.string.str_nav_all_note));
        displayContentView(R.id.nav_all_note);
        navigation = R.id.nav_all_note;

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = null;
                if (navigation == R.id.nav_category) {
                    intent = new Intent(getApplicationContext(), ActivityCategoryEdit.class);
                } else {
                    intent = new Intent(getApplicationContext(), ActivityNoteEdit.class);
                }
                startActivity(intent);
            }
        });

        // for system bar in lollipop
        Tools.systemBarLolipop(this);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        ((TextView) findViewById(R.id.date)).setText(Tools.getNowDate());
    }

    private void initDrawerMenu() {
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                updateDrawerCounter();
                hideKeyboard();
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                drawer.closeDrawers();
                setToolbarTitle(menuItem.getTitle().toString());
                showInterstitial();
                displayContentView(menuItem.getItemId());
                return true;
            }
        });
        user_name = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_name);
        (navigationView.getHeaderView(0).findViewById(R.id.lyt_edit_name)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogEditUserName();
            }
        });
        if (sharedPref.isNameNeverEdit()) {
            dialogEditUserName();
        } else {
            user_name.setText(sharedPref.getUserName());
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (!drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.openDrawer(GravityCompat.START);
        } else {
            doExitApp();
        }
    }

    @Override
    protected void onResume() {
        updateDrawerCounter();
        super.onResume();
    }

    private void updateDrawerCounter() {
        setMenuAdvCounter(R.id.nav_all_note, db.getAllNotes().size());
        setMenuAdvCounter(R.id.nav_fav, db.getAllFavNote().size());
    }

    //set counter in drawer
    private void setMenuAdvCounter(@IdRes int itemId, int count) {
        TextView view = (TextView) navigationView.getMenu().findItem(itemId).getActionView().findViewById(R.id.counter);
        view.setText(String.valueOf(count));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void displayContentView(int id) {
        Fragment fragment = null;
        switch (id) {
            case R.id.nav_all_note:
                navigation = R.id.nav_all_note;
                fragment = new FragmentNote();
                break;
            case R.id.nav_category:
                navigation = R.id.nav_category;
                fragment = new FragmentCategory();
                break;
            case R.id.nav_fav:
                fragment = new FragmentFavorites();
                break;
            case R.id.nav_rate:
                Snackbar.make(parent_view, "Rate This App", Snackbar.LENGTH_SHORT).show();
                Tools.rateAction(this);
                break;
            case R.id.nav_about:
                fragment = new FragmentAbout();
                break;
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_content, fragment);
            fragmentTransaction.commit();
        }
    }

    public void setToolbarTitle(String title) {
        actionBar.setTitle(title);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private long exitTime = 0;

    public void doExitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Snackbar.make(parent_view, R.string.press_again_exit_app, Snackbar.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            db.close();
            finish();
        }
    }

    private void prepareAds() {

        // Create the InterstitialAd and set the adUnitId.
//        mInterstitialAd = new InterstitialAd(this);
//         Defined in res/values/strings.xml
//        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
//        prepare ads
//        AdRequest adRequest2 = new AdRequest.Builder().build();
//        mInterstitialAd.loadAd(adRequest2);
    }

    /**
     * show ads
     */
    public void showInterstitial() {
        // Show the ad if it's ready
//        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
//            mInterstitialAd.show();
//        }
    }

    private void dialogEditUserName() {
        final Dialog dialog = new Dialog(ActivityMain.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_name_edit);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        sharedPref.setNameNeverEdit(false);

        final EditText et_name = (EditText) dialog.findViewById(R.id.name);
        et_name.setText(sharedPref.getUserName());
        ((ImageView) dialog.findViewById(R.id.img_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        ((Button) dialog.findViewById(R.id.bt_save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!et_name.getText().toString().trim().equals("")) {
                    sharedPref.setUserName(et_name.getText().toString());
                    user_name.setText(sharedPref.getUserName());
                    Snackbar.make(parent_view, "Name successfully changed", Snackbar.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Snackbar.make(parent_view, "Name cannot empty", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

}
