package gr.tsagi.jekyllforandroid.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.activities.EditPostActivity;
import gr.tsagi.jekyllforandroid.utils.ParsePostData;
import gr.tsagi.jekyllforandroid.utils.ShowLoading;

/**
 * Created by tsagi on 7/5/14.
 */
public  class EntryListFragment extends Fragment {
    public static final String ARG_PDSTATUS = "post_or_draft";
    public static final String ARG_REPO = "user_repo";

    ListAdapter adapter;

    ShowLoading loadAnim;

    public EntryListFragment() {
        // Empty constructor required for fragment subclasses
    }

    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_posts_list, container, false);
        int i = getArguments().getInt(ARG_PDSTATUS);
        String repo = getArguments().getString(ARG_REPO);
        String type = getResources().getStringArray(R.array.nav_array)[i];

        getActivity().setTitle(type);
        if(i==0)
            new ParsePostData().execute("http://"+ repo + "/json", getActivity());
        return rootView;
    }

    public void updateList(ArrayList<HashMap<String, String>> postList, final List<String> dates,
                           final List<String> urls) {
//        loadAnim.showProgress(PostsListActivity.this,false);
        ListView postsList=(ListView) rootView;
        adapter = new SimpleAdapter(getActivity(), postList,
                R.layout.list_view,
                new String[] { "published_on", "title" }, new int[] {
                R.id.pdate, R.id.ptitle });

        postsList.setAdapter(adapter);
        postsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent editIntent = new Intent(getActivity(), EditPostActivity.class);
                if (!urls.isEmpty()){
                    editIntent.putExtra("post", urls.get(position));
                    editIntent.putExtra("postdate", dates.get(position));
                    startActivity(editIntent);
                }
            }
        });
    }
}
