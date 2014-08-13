package gr.tsagi.jekyllforandroid.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
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
import java.util.concurrent.ExecutionException;

import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.activities.EditPostActivity;
import gr.tsagi.jekyllforandroid.github.GithubPush;
import gr.tsagi.jekyllforandroid.utils.BusProvider;
import gr.tsagi.jekyllforandroid.utils.ParsePostData;
import gr.tsagi.jekyllforandroid.utils.ShowLoading;

/**
 * Created by tsagi on 7/5/14.
 */
public  class PostsListFragment extends Fragment {
    public static final String ARG_PDSTATUS = "post_or_draft";
    public static final String ARG_REPO = "user_repo";

    String mUsername;
    String mToken;
    private String mRepo;
    private SharedPreferences settings;

    ListAdapter adapter;
    ShowLoading loadAnim;

    ParsePostData parsePostData = new ParsePostData();

    View rootView;

    public PostsListFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_posts_list,
                container, false);
        int i = getArguments().getInt(ARG_PDSTATUS);
        String repo = getArguments().getString(ARG_REPO);
        String type = getResources().getStringArray(R.array.nav_array)[i];

        getActivity().setTitle(type);
        if(i==0)
            parsePostData.execute("http://"+ repo + "/json",
                    getActivity());
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        restorePreferences();
        BusProvider.getInstance().register(PostsListFragment.this);
    }

    @Override
    public void onStop() {
        super.onStop();
        BusProvider.getInstance().unregister(PostsListFragment.this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parsePostData.cancel(true);
    }

    public void updateList(ArrayList<HashMap<String, String>> postList,
                           final List<String> dates,
                           final List<String> urls) {
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
                if (!urls.isEmpty()) {
                    editIntent.putExtra("post", urls.get(position));
                    editIntent.putExtra("postdate", dates.get(position));
                    startActivity(editIntent);
                }
            }
        });
    }

    public void uploadJsonTool() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());

        builder.setMessage(R.string.dialog_push_json);
        // Add the buttons
        builder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                /**
                 * Publish post
                 */
                uploadJson();
            }
        });
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void uploadJson() {
        loadAnim.showProgress(this.getActivity(), true);
        GithubPush githubPush = new GithubPush(this.getActivity());
        try {
            githubPush.pushJson();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void pushResult(String result) {
        //loadAnim.showProgress(PostsListActivity.this, false);
        String message;
        if (result.equals("OK")) {
            message = getString(R.string.success);
        } else
            message = getString(R.string.fail);
        Toast.makeText(this.getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void restorePreferences() {
        settings = this.getActivity().getSharedPreferences(
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);
        mUsername = settings.getString("user_login", "");
        mToken = settings.getString("user_status", "");
        mRepo = settings.getString("user_repo", "");

    }


    @Subscribe
    public void dumpOutput(HashMap<String, Object> output) {
        if (output.get("error") != null)
            uploadJsonTool();
        if (output.get("result") != null)
            pushResult((String) output.get("result"));
        if (output.get("postsList") != null) {
            updateList((ArrayList<HashMap<String, String>>) output.get("postsList"),
                    (List<String>) output.get("dates"),
                    (List<String>) output.get("urls"));
        }
    }
}
