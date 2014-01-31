package gr.tsagi.jekyllforandroid.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gr.tsagi.jekyllforandroid.activities.PostsListActivity;

/**
 * Created by tsagi on 1/30/14.
 */

public  class ParsePostData extends AsyncTask<Object, Boolean, String> {

    PostsListActivity postsListActivity;

    @Override
    protected String doInBackground(Object... params) {
        String serviceUrl = (String) params[0];
        postsListActivity = (PostsListActivity) params[1];

        BasicWebService webService = new BasicWebService(serviceUrl);
        return webService.webGet();
    }

    @Override
    protected void onPostExecute(String response) {

        try{
            SharedPreferences settings = postsListActivity.getSharedPreferences(
                    "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);
            String subdir = settings.getString("posts_subdir", "");
            if(!subdir.equals(""))
                subdir = subdir +"/";
            HtmlToJson jsonResult = new HtmlToJson(
                    settings.getString("user_login", ""),
                    settings.getString("user_repo", ""),
                    subdir);
            JSONArray jsonArray = jsonResult.getPostsJson(response);
            ArrayList<HashMap<String,String>> postsList = jsonResult.getPostsArray(jsonArray);
            List<String> dates = jsonResult.getDates(jsonArray);
            List<String> urls = jsonResult.getUrls(jsonArray);
            postsListActivity.updateList(postsList, dates, urls);
        }
        catch(Exception e)
        {
            Log.d("Error: ", " " + e.getMessage());
        }
        super.onPostExecute(response);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
}

