package gr.tsagi.jekyllforandroid.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.Tree;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.activities.EditPostActivity;

/**
 * Created by tsagi on 7/5/14.
 */
public  class DraftsListFragment extends Fragment {
    public static final String ARG_PDSTATUS = "post_or_draft";
    public static final String ARG_REPO = "user_repo";

    ListAdapter adapter;

    private String user;
    private String token;
    private String repo;
    private String dir;
    Tree draftsTree = null;

    List<Map<String, String>> draftsList = new ArrayList<Map<String,String>>();
    List<String> urls = new ArrayList<String>();
    public DraftsListFragment() {
        // Empty constructor required for fragment subclasses
    }

    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SharedPreferences settings = getActivity()
                .getSharedPreferences("gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);
        user = settings.getString("user_login", "");
        token = settings.getString("user_status", "");
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        dir = sharedPref.getString("posts_subdir", "");
        if(!dir.equals(""))
            dir = dir +"/";
        repo = settings.getString("user_repo", "");
        rootView = inflater.inflate(R.layout.fragment_posts_list, container, false);
        int i = getArguments().getInt(ARG_PDSTATUS);
        String repo = getArguments().getString(ARG_REPO);
        String type = getResources().getStringArray(R.array.nav_array)[i];

        getActivity().setTitle(type);
        if(i==1)
            new TreeGet().execute();
        return rootView;
    }

    private class TreeGet extends AsyncTask<Void, Void, Void> {

        String Token = "";
        String user = "";
        Tree baseTree = null;


        @Override
        protected Void doInBackground(Void... args) {
            SharedPreferences settings = getActivity()
                    .getSharedPreferences("gr.tsagi.jekyllforandroid",
                            Context.MODE_PRIVATE);
            user = settings.getString("user_login", "");
            token = settings.getString("user_status", "");
            SharedPreferences sharedPref = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            dir = sharedPref.getString("posts_subdir", "");
            if(!dir.equals(""))
                dir = dir +"/";
            repo = settings.getString("user_repo", "");

            GitHubClient client = new GitHubClient();
            client.setOAuth2Token(token);

            // create needed services
            RepositoryService repositoryService = new RepositoryService();
            CommitService commitService = new CommitService(client);
            DataService dataService = new DataService(client);

            // get some sha's from current state in git
            Log.d("repository", user + "  " + repo);
            Repository repository;
            String baseCommitSha = null;

            try {
                repository =  repositoryService.getRepository(user, repo);
                baseCommitSha = repositoryService.getBranches(repository).get(0).getCommit().getSha();
                RepositoryCommit baseCommit = commitService.getCommit(repository, baseCommitSha);
                String treeSha = baseCommit.getSha();

                baseTree = dataService.getTree(repository, treeSha);
                List<TreeEntry> list = baseTree.getTree();
                String dPos = null;
                for(int i =0; i<list.size(); i++) {
                    if (list.get(i).getPath().equals("_drafts"))
                        dPos = list.get(i).getSha();
                }

                draftsTree = dataService.getTree(repository, dPos);

            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            List<TreeEntry> list = draftsTree.getTree();
            for(int i =0; i<list.size(); i++) {
                Log.d("TreeSub", list.get(i).getPath());
                String draft = list.get(i).getPath();
                draftsList.add(createDraft("draft", draft));
                urls.add("https://raw.githubusercontent" +
                        ".com/" + user + "/" + repo + "/master/_drafts/" +
                        draft);
            }

            updateList(draftsList,urls);

        }
    }
    private HashMap<String, String> createDraft(String key, String name) {
        HashMap<String, String> draft = new HashMap<String, String>();
        draft.put(key, name);

        return draft;
    }

    private HashMap<String, String> createUrl(String key, String name) {
        HashMap<String, String> url = new HashMap<String, String>();
        url.put(key, name);

        return url;
    }

    public void updateList(List<Map<String, String>> draftsList,
                           final List<String> urls) {
        ListView postsList=(ListView) rootView;
        adapter = new SimpleAdapter(getActivity(), draftsList,
                R.layout.drafts_list_view,
                new String[] { "draft" }, new int[] { R.id.dtitle });

        postsList.setAdapter(adapter);
        postsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent editIntent = new Intent(getActivity(),
                        EditPostActivity.class);
                if (!urls.isEmpty()){
                    editIntent.putExtra("post", urls.get(position));
                    startActivity(editIntent);
                }
            }
        });
    }
}
