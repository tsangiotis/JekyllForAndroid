package gr.tsagi.jekyllforandroid.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static gr.tsagi.jekyllforandroid.app.utils.LogUtils.LOGD;

/**
 * Created by tsagi on 1/29/14.
 */

public class JekyllRepo {

    private Context context;
    private String jsonList;


    public String getName(String user, Context c){

        context = c;

        try{
            return new CheckAllRepos().execute(user).get();
        }catch (InterruptedException e) {

            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    private class CheckAllRepos extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {

            String user = params[0];
            String name = null;

            Map<String, String> refs = new HashMap<String, String>();
            List<RepositoryBranch> branches;

            RepositoryService repositoryService = new RepositoryService();

            List<Repository> repositories = null;
            try {
                repositories = repositoryService.getRepositories(user);
            } catch (IOException e) {
                e.printStackTrace();
                return "norepos";
            }
            LOGD("JEKYLLREPO", repositories.toString());
            LOGD("JEKYLLREPO", user);
            for (Repository repository : repositories) {

                LOGD("JEKYLLREPO", repository.getName());

                // Exclude forked repositories
                if (!repository.isFork()) {
                    try {
                        LOGD("JEKYLLREPO", repository.getName());
                        repository.getOwner();
                        branches = new RepositoryService().getBranches(repository);
                        for (RepositoryBranch branch : branches) {
                            if (branch.getName().equals("gh-pages")) {
                                refs.put(repository.getName(), branch.getName());
                            }
                            if (repository.getName().contains(user + ".github.")) {
                                refs.put(repository.getName(), repository.getMasterBranch());
                                name = repository.getName();
                                break;
                            }
                            if (repository.getName().contains(user.toLowerCase() + ".github.")) {
                                refs.put(repository.getName(), repository.getMasterBranch());
                                name = repository.getName();
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            jsonList = new Gson().toJson(refs);
            Log.d("JekyllRepo", jsonList);

            return jsonList;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(!s.equals("norepos")) {
                SharedPreferences prefs = context.getSharedPreferences("gr.tsagi.jekyllforandroid",
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("branchesJSON", s);
                editor.putString("currentRepo", s);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    editor.apply();
                } else {
                    editor.commit();
                }
            }

        }
    }
}
