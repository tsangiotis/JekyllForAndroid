package gr.tsagi.jekyllforandroid.github;

import android.os.AsyncTask;

import gr.tsagi.jekyllforandroid.activities.EditPostActivity;
import gr.tsagi.jekyllforandroid.utils.BasicWebService;
import gr.tsagi.jekyllforandroid.utils.ShowPost;

/**
 * Created by tsagi on 1/31/14.
 */

public class GithubRaw extends AsyncTask<Object, Boolean, String[]> {

    EditPostActivity postActivity;

    @Override
    protected String[] doInBackground(Object... params) {
        String serviceUrl = (String) params[0];
        postActivity = (EditPostActivity) params[1];


        BasicWebService webService = new BasicWebService(serviceUrl);
        String[] response =  webService.webGetPost();
        return response;
    }

    @Override
    protected void onPostExecute(String... responses) {

        String yaml = responses[0];
        String content = responses[1];
        postActivity.getThePost(new ShowPost().yamlFromString(yaml, content));
        super.onPostExecute(responses);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }
}
