package com.jchanghong


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.jchanghong.data.DatabaseManager
import com.jchanghong.data.SharedPref
import com.jchanghong.fragment.FragmentAbout
import com.jchanghong.fragment.FragmentCategory
import com.jchanghong.fragment.FragmentFavorites
import com.jchanghong.fragment.FragmentNote
import com.jchanghong.utils.Tools

class ActivityMain : AppCompatActivity() {

    //for ads

   lateinit private var toolbar: Toolbar 
  lateinit   var actionBar: ActionBar
  lateinit  private var navigationView: NavigationView 
   lateinit private var floatingActionButton: FloatingActionButton 
 lateinit   private var parent_view: View 
  lateinit  private var user_name: TextView 

  lateinit  private var db: DatabaseManager 
  lateinit  private var sharedPref: SharedPref 

    private var navigation = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        parent_view = findViewById(android.R.id.content)

        floatingActionButton = findViewById(R.id.fab) as FloatingActionButton

        sharedPref = SharedPref(this)
        db = GlobalApplication.db // init db

        prepareAds()
        initToolbar()
        initDrawerMenu()
        // cek cek

        // set home view
        actionBar.title = getString(R.string.str_nav_all_note)
        displayContentView(R.id.nav_all_note)
        navigation = R.id.nav_all_note

        floatingActionButton .setOnClickListener {
            var intent: Intent
            if (navigation == R.id.nav_category) {
                intent = Intent(applicationContext, ActivityCategoryEdit::class.java)
            } else {
                intent = Intent(applicationContext, ActivityNoteEdit::class.java)
            }
            startActivity(intent)
        }

        // for system bar in lollipop
        Tools.systemBarLolipop(this)
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        (findViewById(R.id.date) as TextView).text = Tools.nowDate
    }

    private fun initDrawerMenu() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = object : ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            override fun onDrawerOpened(drawerView: View) {
                updateDrawerCounter()
                hideKeyboard()
                super.onDrawerOpened(drawerView)
            }
        }
        drawer.setDrawerListener(toggle)
        toggle.syncState()

        navigationView = findViewById(R.id.nav_view) as NavigationView
        navigationView .setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawer.closeDrawers()
            setToolbarTitle(menuItem.title.toString())
            showInterstitial()
            displayContentView(menuItem.itemId)
            true
        }
        user_name = navigationView .getHeaderView(0).findViewById<View>(R.id.user_name) as TextView
        navigationView .getHeaderView(0).findViewById<View>(R.id.lyt_edit_name).setOnClickListener { dialogEditUserName() }
        if (sharedPref .isNameNeverEdit) {
            dialogEditUserName()
        } else {
            user_name .text = sharedPref .userName
        }
    }

    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (!drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.openDrawer(GravityCompat.START)
        } else {
            doExitApp()
        }
    }

    override fun onResume() {
        updateDrawerCounter()
        super.onResume()
    }

    private fun updateDrawerCounter() {
        setMenuAdvCounter(R.id.nav_all_note, db .allNotes.size)
        setMenuAdvCounter(R.id.nav_fav, db .allFavNote.size)
    }

    //set counter in drawer
    private fun setMenuAdvCounter(@IdRes itemId: Int, count: Int) {
        val view = navigationView .menu.findItem(itemId).actionView.findViewById<View>(R.id.counter) as TextView
        view.text = count.toString()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    private fun displayContentView(id: Int) {
        var fragment: Fragment? = null
        when (id) {
            R.id.nav_all_note -> {
                navigation = R.id.nav_all_note
                fragment = FragmentNote()
            }
            R.id.nav_category -> {
                navigation = R.id.nav_category
                fragment = FragmentCategory()
            }
            R.id.nav_fav -> fragment = FragmentFavorites()
            R.id.nav_rate -> {
                Snackbar.make(parent_view, getString(R.string.ratethisapp), Snackbar.LENGTH_SHORT).show()
                Tools.rateAction(this)
            }
            R.id.nav_about -> fragment = FragmentAbout()
        }
        if (fragment != null) {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frame_content, fragment)
            fragmentTransaction.commit()
        }
    }

    fun setToolbarTitle(title: String) {
        actionBar.title = title
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private var exitTime: Long = 0

    fun doExitApp() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            Snackbar.make(parent_view, R.string.press_again_exit_app, Snackbar.LENGTH_SHORT).show()
            exitTime = System.currentTimeMillis()
        } else {
            db .close()
            finish()
        }
    }

    private fun prepareAds() {

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
    fun showInterstitial() {
        // Show the ad if it's ready
        //        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
        //            mInterstitialAd.show();
        //        }
    }

    private fun dialogEditUserName() {
        val dialog = Dialog(this@ActivityMain)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_name_edit)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window .attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT

        sharedPref .isNameNeverEdit = false

        val et_name = dialog.findViewById<View>(R.id.name) as EditText
        et_name.setText(sharedPref .userName)
        (dialog.findViewById<View>(R.id.img_close) as ImageView).setOnClickListener { dialog.dismiss() }
        (dialog.findViewById<View>(R.id.bt_save) as Button).setOnClickListener {
            if (et_name.text.toString().trim { it <= ' ' } != "") {
                sharedPref .userName = et_name.text.toString()
                user_name .text = sharedPref .userName
                Snackbar.make(parent_view, getString(R.string.name_changed), Snackbar.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Snackbar.make(parent_view, getString(R.string.namecannotempty), Snackbar.LENGTH_SHORT).show()
            }
        }
        dialog.show()
        dialog.window .attributes = lp
    }

}
