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

public class ParsePostData extends AsyncTask<Object, Boolean, String> {

    PostsListActivity postsListActivity;

    @Override
    protected String doInBackground(Object... params) {
        String serviceUrl = (String) params[0];
        postsListActivity = (PostsListActivity) params[1];

        BasicWebService webService = new BasicWebService(serviceUrl);
        String response =  webService.webGetJson();
        return response;
    }

    @Override
    protected void onPostExecute(String response) {

        BusProvider.getInstance().register(this);
        BusProvider.getInstance().post(response);
        BusProvider.getInstance().unregister(this);

        SharedPreferences settings = postsListActivity.getSharedPreferences(
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);

        ArrayList<HashMap<String,String>> postsList;
        List<String> dates;
        List<String> urls;
        String source;
        Log.d("response", response);
        if(response.equals("IOerror")){
           Log.d("response", "get in the if mfk");
           source = settings.getString("old_json", "{\"posts\":[{\"url\":\"\",\"title\":" +
                   "\"No Connection and no old data available\",\"id\": \"nodata\"," +
                   "\"published_on\":\"Tried getting online?\"}]}");
        }
        else{
            source = response;
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("old_json", response);
            editor.commit();
        }
        try{
            String subdir = settings.getString("posts_subdir", "");
            if(!subdir.equals(""))
                subdir = subdir +"/";
            HtmlToJson jsonResult = new HtmlToJson(
                    settings.getString("user_login", ""),
                    settings.getString("user_repo", ""),
                    subdir);
            JSONArray jsonArray = jsonResult.getPostsJson(source);
            if(jsonArray != null){
            postsList = jsonResult.getPostsArray(jsonArray);
            dates = jsonResult.getDates(jsonArray);
            urls = jsonResult.getUrls(jsonArray);
            postsListActivity.updateList(postsList, dates, urls);
            }
            else
                postsListActivity.uploadJsonTool();
        }
        catch(Exception e)
        {
            Log.d("Error: ", " " + e.getMessage());
        }
    }

}

