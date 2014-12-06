package gr.tsagi.jekyllforandroid.app.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Base64;
import android.util.Log;

import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import gr.tsagi.jekyllforandroid.app.data.PostsContract.PostEntry;
import gr.tsagi.jekyllforandroid.app.model.Post;


/**
 * Created by tsagi on 8/15/14.
 */
public class ParsePostData {

    private final String LOG_TAG = ParsePostData.class.getSimpleName();
    private final Context mContext;

    public ParsePostData(Context context) {
        mContext = context;
    }


    final String JK_TITLE = "title";
    final String JK_CATEGORY = "category";
    final String JK_TAGS = "tags";


    public ContentValues getDataFromContent(String id, String contentBytes, int type) {

        // Get and insert the new posts information into the database
        String postContent = null;

        // Blobs return with Base64 encoding so we have to UTF-8 them.
        byte[] bytes = Base64.decode(contentBytes, Base64.DEFAULT);
        try {
            postContent = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        StringBuilder stringBuilder = new StringBuilder();

        InputStream is;
        BufferedReader r;

        is = new ByteArrayInputStream(postContent.getBytes());
        // read it with BufferedReader
        r = new BufferedReader(new InputStreamReader(is));
        String line;

        int yaml_dash = 0;
        String yamlStr = "";
        try {
            while ((line = r.readLine()) != null) {
                if (line.equals("---")) {
                    yaml_dash++;
                }
                if (yaml_dash < 2) {
                    if (!line.equals("---"))
                        yamlStr = yamlStr + line + "\n";
                }
                if (yaml_dash >= 2) {
                    if (!line.equals("---"))
                        if (line.equals(""))
                            stringBuilder.append("\n");
                        else
                            stringBuilder.append(line);
                }
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Post post = new Post();

        Yaml yaml = new Yaml();

        Map map;
        map = (Map) yaml.load(yamlStr);

        post.id = id;

        post.setTitle(map.get(JK_TITLE).toString());


        String tags = null;

        if (map.containsKey(JK_TAGS)) {
             tags = map.get(JK_TAGS).toString().replace("[", "").replace("]", "");
        }
        else {
            tags = "null";
        }
        post.setTags(tags);

        if (map.containsKey(JK_CATEGORY)) {
            post.setCategory(map.get(JK_CATEGORY).toString());
        } else {
            post.setCategory("null");
        }

        post.setContent(stringBuilder.toString());

        String date = "";

        if (type == 0) {
            post.noDraft();
            int i = post.getId().indexOf('-', 1 + id.indexOf('-', 1 + id.indexOf('-')));
            date = post.getId().substring(0, i).replace("-", "");
            post.setDate(date);
        } else {
            post.isDraft();
            post.setDate("");
        }

        // First, check if the post exists in the db
        Cursor cursorId = mContext.getContentResolver().query(
                PostEntry.CONTENT_URI,
                new String[]{PostEntry.COLUMN_POST_ID},
                PostEntry.COLUMN_POST_ID + " = ?",
                new String[]{post.getId()},
                null);

        Cursor cursorContent = mContext.getContentResolver().query(
                PostEntry.CONTENT_URI,
                new String[]{PostEntry.COLUMN_CONTENT},
                PostEntry.COLUMN_CONTENT + " = ?",
                new String[]{post.getContent()},
                null);

        Cursor cursorTags = mContext.getContentResolver().query(
                PostEntry.CONTENT_URI,
                new String[]{PostEntry.COLUMN_TAGS},
                PostEntry.COLUMN_TAGS + " = ?",
                new String[]{post.getTags()},
                null);

        Cursor cursorCategory = mContext.getContentResolver().query(
                PostEntry.CONTENT_URI,
                new String[]{PostEntry.COLUMN_CATEGORY},
                PostEntry.COLUMN_CATEGORY + " = ?",
                new String[]{post.getCategory()},
                null);

        ContentValues postValues = new ContentValues();

        if (cursorId.moveToFirst()) {
            cursorId.close();
            ContentValues updateValues = new ContentValues();
            if (!cursorContent.moveToFirst()) {
                cursorContent.close();
                updateValues.put(PostEntry.COLUMN_CONTENT, post.getContent());
            }
            if (!cursorTags.moveToFirst()) {
                cursorTags.close();
                updateValues.put(PostEntry.COLUMN_TAGS, post.getTags());
            }
            if (!cursorCategory.moveToFirst()) {
                cursorCategory.close();
                updateValues.put(PostEntry.COLUMN_CATEGORY, post.getCategory());
            }
            if (updateValues.size() > 0) {
                mContext.getContentResolver().update(PostEntry.CONTENT_URI, updateValues,
                        PostEntry.COLUMN_POST_ID + " = \"" + post.getId() + "\"", null);
            }
        } else {
            cursorId.close();
            cursorContent.close();
            cursorTags.close();
            cursorCategory.close();

            Log.d(LOG_TAG, post.getId());

            postValues.put(PostEntry.COLUMN_TITLE, post.getTitle());
            if (!date.equals("")) {
                postValues.put(PostEntry.COLUMN_DATETEXT, post.getDate());
            }
            postValues.put(PostEntry.COLUMN_DRAFT, post.getStatus());
            postValues.put(PostEntry.COLUMN_CONTENT, post.getContent());
            postValues.put(PostEntry.COLUMN_TAGS, post.getTags());
            postValues.put(PostEntry.COLUMN_CATEGORY, post.getCategory());
            postValues.put(PostEntry.COLUMN_POST_ID, post.getId());
        }

        return postValues;

    }

}
