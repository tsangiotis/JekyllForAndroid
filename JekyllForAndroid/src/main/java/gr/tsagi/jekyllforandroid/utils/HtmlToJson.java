package gr.tsagi.jekyllforandroid.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tsagi on 1/30/14.
 */

public  class HtmlToJson {

    private String user;
    private String repo;
    private String dir;

    public HtmlToJson(String jUser, String jRepo, String jDir){
        user = jUser;
        repo = jRepo;
        dir = jDir;
    }

    public JSONArray getPostsJson (String json_html){
        JSONObject json;
        JSONArray posts = null;

        try {
            json = new JSONObject(json_html);
            // Getting Array of Readings
            posts = json.getJSONArray("posts");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return posts;
    }

    public List<String> getDates(JSONArray posts) throws JSONException {

        List<String> dates = new ArrayList<String>();
        for(int i = 0; i < posts.length(); i++){
            JSONObject r = posts.getJSONObject(i);

            String separatedId[] = r.getString("id").split("/");
            for(int j=0; j < separatedId.length; j++){
                if(separatedId[j].startsWith("20")){
                    dates.add(separatedId[j] + "-" + separatedId[j + 1] + "-" + separatedId[j + 2]);
                    break;
                }
            }
        }
        return dates;
    }

    public List<String> getUrls(JSONArray posts) throws JSONException{
        List<String> urls = new ArrayList<String>();
        for(int i = 0; i < posts.length(); i++){
            JSONObject r = posts.getJSONObject(i);

            String separatedId[] = r.getString("id").split("/");
            for(int j=0; j < separatedId.length; j++){
                if(separatedId[j].startsWith("20")){
                    String url = "https://raw.github.com/" + user + "/" + repo +
                            "/master/_posts/" + dir +
                            separatedId[j] +"-"+ separatedId[j+1] +"-" +
                            separatedId[j+2] + "-"+separatedId[j+3]+".md";
                    urls.add(url);
                    break;
                }
            }
        }
        return urls;


    }

    public ArrayList<HashMap<String, String>> getPostsArray(JSONArray posts){

        HashMap<String, String> map;

        ArrayList<HashMap<String, String>> postList = new ArrayList<HashMap<String, String>>();

        try {
            for(int i = 0; i < posts.length(); i++){
                JSONObject r = posts.getJSONObject(i);

                map = new HashMap<String, String>();
                // adding each child node to HashMap key => value
                map.put("published_on", r.getString("published_on"));
                map.put("title", r.getString("title"));
                Log.d("jsonising", r.getString("title"));

                // adding HashList to ArrayList
                postList.add(map);
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return postList;
    }
}