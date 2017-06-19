package gr.tsagi.jekyllforandroid.app.utils;

import android.os.AsyncTask;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by tsagi on 1/29/14.
 */

public class JekyllRepo {

    public String getName(String user){

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

            RepositoryService repositoryService = new RepositoryService();

            List<Repository> repositories = null;
            try {
                repositories = repositoryService.getRepositories(user);
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (Repository repository : repositories) {
                if (repository.getName().contains(user + ".github.")) {
                    name = repository.getName();
                    break;
                }
                if (repository.getName().contains(user.toLowerCase() + ".github.")) {
                    name = repository.getName();
                    break;
                }
            }
            return name;
        }

    }
}
