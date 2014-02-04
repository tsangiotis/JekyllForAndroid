package gr.tsagi.jekyllforandroid.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

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
        return webService.webGetJson();
    }

    @Override
    protected void onPostExecute(String response) {
        Map<String, Object> result = new HashMap<String, Object>();

        SharedPreferences settings = postsListActivity.getSharedPreferences(
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);

        String source;
        if(response.equals("404"))
            result.put("error", "There was an error");
        if(response.equals("IOerror")){
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
        if(!response.equals("404")){
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
                    result.put("postsList", jsonResult.getPostsArray(jsonArray));
                    result.put("dates", jsonResult.getDates(jsonArray));
                    result.put("urls", jsonResult.getUrls(jsonArray));
                }
                else{
                    Log.e("JsonArrayNull", null);
                }
            }
            catch(Exception e)
            {
                Log.d("Error: ", " " + e.getMessage());
            }
        }
        register(result);

    }

    private void register(Map<String,Object> result){
        BusProvider.getInstance().register(this);
        BusProvider.getInstance().post(result);
        BusProvider.getInstance().unregister(this);
    }

}

