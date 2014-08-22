package gr.tsagi.jekyllforandroid.utils;

import android.os.AsyncTask;
import android.util.Log;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by tsagi on 1/29/14.
 */

public class JekyllRepo {

<<<<<<< HEAD
    public String getName(String user) {
        String name;

        try {
            name = new CheckAllRepos().execute(user).get();
            return name;
        } catch (InterruptedException e) {
=======
    public String getName(String user){

        try{
            return new CheckAllRepos().execute(user).get();
        }catch (InterruptedException e) {
>>>>>>> release/v1.5.5
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

            RepositoryService repositoryService = new RepositoryService();

            List<Repository> repositories = null;
            try {
                repositories = repositoryService.getRepositories(user);
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (Repository repository : repositories) {
<<<<<<< HEAD
                if (repository.getName().contains(user + ".github.")) {
=======
                Log.d("JekyllRepo", repository.getName());
                if (repository.getName().contains(user + ".github.")) {
                    Log.d("JekyllRepo", "Selected" + repository.getName());
>>>>>>> release/v1.5.5
                    name = repository.getName();
                    break;
                }
                if (repository.getName().contains(user.toLowerCase() + ".github.")) {
<<<<<<< HEAD
=======
                    Log.d("JekyllRepo", "Selected" + repository.getName());
>>>>>>> release/v1.5.5
                    name = repository.getName();
                    break;
                }
            }
            return name;
        }

    }
}
