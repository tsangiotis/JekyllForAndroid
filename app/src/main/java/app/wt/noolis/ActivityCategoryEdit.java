package app.wt.noolis;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import java.util.List;

import app.wt.noolis.adapter.ListAdapterCategoryIcon;
import app.wt.noolis.data.DatabaseManager;
import app.wt.noolis.model.Category;
import app.wt.noolis.model.CategoryIcon;

/**
 * Created by Kodok on 12/06/2016.
 */
public class ActivityCategoryEdit extends AppCompatActivity{
    public static final String EXTRA_OBJCT = "app.wt.noolis.EXTRA_OBJECT_CATEGORY";
    public static final int OPEN_DIALOG_CATEGORY_CODE = 1;

    private View parent_view;
    private boolean is_new = true;
    private Category ext_cat = null;
    private Button btnSave;
    private EditText txtTittle;
    private RadioButton radioIcon;

    private DatabaseManager db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_add);
        parent_view = findViewById(android.R.id.content);

        //init component
        initComponent();

        // init db
        db = new DatabaseManager(this);

        hideKeyboard();

        // get extra object
        ext_cat = (Category) getIntent().getSerializableExtra(EXTRA_OBJCT);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.category_icon_list);
        recyclerView.setHasFixedSize(true);

        //grid layout
        GridLayoutManager lLayout = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(lLayout);
        List<CategoryIcon> icons = db.getCategoryIcon();
        final ListAdapterCategoryIcon ai = new ListAdapterCategoryIcon(getApplicationContext(), icons);
        recyclerView.setAdapter(ai);

        //define field from object
        if (ext_cat != null) {
            txtTittle.setText(ext_cat.getName());
            ai.setSelectedRadio(ext_cat.getIcon());
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtTittle.getText().toString().equals("") || ai.getSelectedCategoryIcon() == null) {
                    Toast.makeText(getApplicationContext(), "Category Name or Icon can't be empty", Toast.LENGTH_SHORT).show();
                } else {
                    if (ext_cat != null) {
                        if (ext_cat.getName().equals(txtTittle.getText().toString()) && ext_cat.getIcon().equals(ai.getSelectedCategoryIcon().getIcon())) {
                            finish();
                        } else {
                            ext_cat.setName(txtTittle.getText().toString());
                            ext_cat.setIcon(ai.getSelectedCategoryIcon().getIcon());
                            ext_cat.setColor(ai.getSelectedCategoryIcon().getColor());
                            db.updateCategory(ext_cat);
                            finish();
                            Toast.makeText(getApplicationContext(), "Category updated", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Category category = new Category();
                        category.setName(txtTittle.getText().toString());
                        category.setColor(ai.getSelectedCategoryIcon().getColor());
                        category.setIcon(ai.getSelectedCategoryIcon().getIcon());
                        db.insertCategory(category);
                        finish();
                        Toast.makeText(getApplicationContext(), "Category saved", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    private void initComponent() {
        txtTittle = (EditText) findViewById(R.id.cat_tittle);
        btnSave = (Button) findViewById(R.id.btn_save);
        radioIcon = (RadioButton) findViewById(R.id.radioSelected);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void actionSave() {
//        hideKeyboard();
//        if (category_name.getText().toString().equals("")) {
//            Snackbar.make(parent_view, "Category name can't be empty", Snackbar.LENGTH_SHORT).show();
//        } else {
//            if(is_new) ext_note =  new Note();
//            String notif_text;
//
//            ext_note.setTittle(category_name.getText().toString());
//            ext_note.setLastEdit(System.currentTimeMillis());
//            ext_note.setCategory(cur_category);
//
//            if(is_new){
//                notif_text = "Note Saved";
//                db.insertNote(ext_note);
//            }else{
//                notif_text = "Note Updated";
//                db.updateNote(ext_note);
//            }
//
//            Snackbar.make(parent_view, notif_text, Snackbar.LENGTH_SHORT).setCallback(new Snackbar.Callback() {
//                @Override
//                public void onDismissed(Snackbar snackbar, int event) {
//                    super.onDismissed(snackbar, event);
//                    finish();
//                }
//            }).show();
//        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void backConfirmation() {
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
