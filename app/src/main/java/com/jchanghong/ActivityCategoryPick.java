package com.jchanghong;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jchanghong.data.DatabaseManager;
import com.jchanghong.model.Category;
import com.jchanghong.utils.Tools;

import java.util.List;

public class ActivityCategoryPick extends AppCompatActivity {


    /** Return Intent extra */
    public static String EXTRA_OBJ = "com.jchanghong.EXTRA_OBJ";

    private AdapterListCategory adapterListCategory;
    private DatabaseManager db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_pick);
        db = new DatabaseManager(this);
        initToolbar();

        adapterListCategory = new AdapterListCategory(this, db.getAllCategory());
        ListView listView = (ListView) findViewById(R.id.paired_devices);
        listView.setAdapter(adapterListCategory);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int i, long l) {
                sendIntentResult((Category) adapterListCategory.getItem(i));
            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setTitle(R.string.title_activity_pick_category);
    }

    private void sendIntentResult(Category category){
        // send extra object
        Intent intent = new Intent();
        intent.putExtra(EXTRA_OBJ, category);

        // Set result and finish this Activity
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private class AdapterListCategory extends BaseAdapter {

        private List<Category> items;
        private Context ctx;

        public AdapterListCategory(Context context, List<Category> items) {
            super();
            this.ctx = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Category obj = (Category) getItem(position);
            ViewHolder holder;
            if(convertView == null){
                holder 				= new ViewHolder();
                convertView			= LayoutInflater.from(ctx).inflate(R.layout.row_category_simple, parent, false);
                holder.image 		= (ImageView) convertView.findViewById(R.id.image);
                holder.name 		= (TextView) convertView.findViewById(R.id.name);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }

            holder.image.setImageResource(Tools.StringToResId(obj.getIcon(),ctx));
            ((GradientDrawable) holder.image.getBackground()).setColor(Color.parseColor(obj.getColor()));
            holder.name.setText(obj.getName());

            return convertView;
        }

        private class ViewHolder {
            ImageView   image;
            TextView    name;
        }
    }

}
