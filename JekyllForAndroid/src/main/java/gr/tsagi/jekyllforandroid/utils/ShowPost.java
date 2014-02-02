package gr.tsagi.jekyllforandroid.utils;

import android.util.Log;

import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;

/**
 * Created by tsagi on 1/31/14.
 */

public class ShowPost{

    public HashMap<String, String> yamlFromString(String yamlStr, String content) {
        Yaml yaml = new Yaml();
        Log.d("yaml", yamlStr);
        HashMap<String, String[]> map = (HashMap<String, String[]>) yaml.load(yamlStr);
        HashMap<String, String> postmap = new HashMap<String, String>();

        postmap.put("title", String.valueOf(map.get("title")));
        postmap.put("category", String.valueOf(map.get("category")));
        postmap.put("tags", String.valueOf(map.get("tags")).replace("[", "").replace("]", ""));
        postmap.put("content", content);

        return postmap;
    }
}
