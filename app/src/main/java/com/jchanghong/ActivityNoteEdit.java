package com.jchanghong;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jchanghong.data.DatabaseManager;
import com.jchanghong.model.Category;
import com.jchanghong.model.Note;
import com.jchanghong.utils.Tools;

import com.jchanghong.R;

public class ActivityNoteEdit extends AppCompatActivity {

    public static final String EXTRA_OBJCT = "com.jchanghong.EXTRA_OBJECT_NOTE";
    public static final int OPEN_DIALOG_CATEGORY_CODE = 1;

    private Toolbar toolbar;
    private ActionBar actionBar;
    private View parent_view;
    private EditText tittle, content;
    private TextView time;
    private ImageView cat_icon, cat_drop;
    private TextView cat_text;
    private AppBarLayout appbar;
    private Menu menu;

    private boolean fav_checked = false;
    private boolean is_new = true;

    private Note ext_note = null;
    private Category cur_category = null;
    private DatabaseManager db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        parent_view = findViewById(android.R.id.content);

        // init db
        db = new DatabaseManager(this);

        initToolbar();

        // get extra object
        ext_note = (Note) getIntent().getSerializableExtra(EXTRA_OBJCT);
        iniComponent();

    }


    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("");
    }


    private void iniComponent() {
        tittle = (EditText) findViewById(R.id.tittle);
        content = (EditText) findViewById(R.id.content);
        time = (TextView) findViewById(R.id.time);

        cat_icon = (ImageView) findViewById(R.id.cat_icon);
        cat_drop = (ImageView) findViewById(R.id.cat_drop);
        cat_text = (TextView) findViewById(R.id.cat_text);
        appbar = (AppBarLayout) findViewById(R.id.appbar);

        is_new = (ext_note == null);

        if (is_new) {
            time.setText("");
            cur_category = db.getFirstCategory();
        } else {
            time.setText("Edited : " + Tools.stringToDate(ext_note.getLastEdit()));
            tittle.setText(ext_note.getTittle());
            content.setText(ext_note.getContent());
            cur_category = ext_note.getCategory();
        }
        setCategoryView(cur_category);

        ((LinearLayout) findViewById(R.id.lyt_category)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ActivityCategoryPick.class);
                startActivityForResult(i, OPEN_DIALOG_CATEGORY_CODE);
            }
        });

        ((Button) findViewById(R.id.bt_save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionSave();
            }
        });
    }

    private void setCategoryView(Category category) {
        cat_icon.setImageResource(Tools.StringToResId(category.getIcon(),getApplicationContext()));
        cat_icon.setColorFilter(Color.parseColor(category.getColor()));
        cat_drop.setColorFilter(Color.parseColor(category.getColor()));
        cat_text.setText(category.getName());
        cat_text.setTextColor(Color.parseColor(category.getColor()));
        appbar.setBackgroundColor(Color.parseColor(category.getColor()));
        Tools.systemBarLolipopCustom(ActivityNoteEdit.this, category.getColor());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_activity_manage_note, menu);
        //set fav icon
        if (!is_new) {
            if (ext_note.getFavourite() == 1) {
                fav_checked = true;
                menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_favorites_solid));
            } else {
                fav_checked = false;
                menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_favorites_outline));
            }
        } else {
            menu.getItem(0).setVisible(false);
            menu.getItem(1).setVisible(false);
        }
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OPEN_DIALOG_CATEGORY_CODE && resultCode == Activity.RESULT_OK) {
            cur_category = (Category) data.getSerializableExtra(ActivityCategoryPick.EXTRA_OBJ);
            setCategoryView(cur_category);
            Snackbar.make(parent_view, "Category Selected : " + cur_category.getName(), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_delete:
                deleteConfirmation();
                break;
            case R.id.action_fav:
                actionFavorite();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void actionFavorite() {
        if (!is_new) {
            if (fav_checked) {
                menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_favorites_outline));
                db.removeFav(ext_note.getId());
                Snackbar.make(parent_view, "Removed from favorites", Snackbar.LENGTH_SHORT).show();
                fav_checked = false;
            } else {
                menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_favorites_solid));
                db.setFav(ext_note.getId());
                Snackbar.make(parent_view, "Added to favorites", Snackbar.LENGTH_SHORT).show();
                fav_checked = true;
            }
        }
    }

    private void actionSave() {
        if (tittle.getText().toString().equals("") || content.getText().toString().equals("")) {
            Snackbar.make(parent_view, "Tittle or Content can't be empty", Snackbar.LENGTH_SHORT).show();
        } else {
            if (is_new) ext_note = new Note();
            String notif_text;

            ext_note.setTittle(tittle.getText().toString());
            ext_note.setContent(content.getText().toString());
            ext_note.setLastEdit(System.currentTimeMillis());
            ext_note.setCategory(cur_category);

            if (is_new) {
                notif_text = "Note Saved";
                db.insertNote(ext_note);
            } else {
                notif_text = "Note Updated";
                db.updateNote(ext_note);
            }

            Snackbar.make(parent_view, notif_text, Snackbar.LENGTH_SHORT).setCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    super.onDismissed(snackbar, event);
                    finish();
                }
            }).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (ext_note != null) {
            if ((!(tittle.getText().toString().equals(ext_note.getTittle()))) ||
                    (!(content.getText().toString().equals(ext_note.getContent()))) ||
                    (!(cur_category.getId() == ext_note.getCategory().getId()))
                    ) {
                backConfirmation();
            } else {
                finish();
            }
        } else {
            if (tittle.getText().toString().equals("") && content.getText().toString().equals("")) {
                //do nothing
                finish();
            } else {
                backConfirmation();
            }
        }
    }

    private void backConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityNoteEdit.this);
        builder.setTitle("Save Confirmation");
        builder.setMessage("Do you want to save?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (ext_note == null) {
                    //save new note
                    Note n = new Note();
                    n.setTittle(tittle.getText() + "");
                    n.setContent(content.getText() + "");
                    n.setCategory(cur_category);
                    n.setFavourite(0);
                    n.setLastEdit(System.currentTimeMillis());
                    db.insertNote(n);
                } else {
                    ext_note.setTittle(tittle.getText() + "");
                    ext_note.setContent(content.getText() + "");
                    ext_note.setCategory(cur_category);
                    //no need to set fav here, fav already save to DB when clicked
                    ext_note.setLastEdit(System.currentTimeMillis());
                    db.updateNote(ext_note);
                    ext_note.clear();
                }
                Snackbar.make(parent_view, "Note Saved", Snackbar.LENGTH_SHORT).show();
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.show();
    }

    private void deleteConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityNoteEdit.this);
        builder.setTitle("Delete Confirmation");
        builder.setMessage("Are you sure want to delete this Note?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (ext_note != null) {//modify
                    db.deleteNote(ext_note.getId());
                }
                Toast.makeText(getApplicationContext(), "Note Deleted", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

}
