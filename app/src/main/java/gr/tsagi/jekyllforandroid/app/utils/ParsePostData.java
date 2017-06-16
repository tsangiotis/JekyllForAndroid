package gr.tsagi.jekyllforandroid.app.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Base64;
import gr.tsagi.jekyllforandroid.app.data.PostsContract.PostEntry;

import java.io.UnsupportedEncodingException;


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

//        StringBuilder stringBuilder = new StringBuilder();
//
//        InputStream is;
//        BufferedReader r;
//
//        is = new ByteArrayInputStream(postContent.getBytes());
//        // read it with BufferedReader
//        r = new BufferedReader(new InputStreamReader(is));
//        String line;
//
//        int yaml_dash = 0;
//        String yamlStr = "";
//        try {
//            while ((line = r.readLine()) != null) {
//                if (line.equals("---")) {
//                    yaml_dash++;
//                }
//                if (yaml_dash < 2) {
//                    if (!line.equals("---"))
//                        yamlStr = yamlStr + line + "\n";
//                }
//                if (yaml_dash >= 2) {
//                    if (!line.equals("---"))
//                        if (line.equals(""))
//                            stringBuilder.append("\n");
//                        else
//                            stringBuilder.append(line);
//                }
//            }
//            is.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


//        String content = stringBuilder.toString();



//        Yaml yaml = new Yaml();

//        Map map;
//        map = (Map) yaml.load(yamlStr);

        String title=id;
        String tags="null";
        String category="null";

//        title = map.get(JK_TITLE).toString();

//        if (map.containsKey(JK_TAGS)) {
//            tags = map.get(JK_TAGS).toString().replace("[", "").replace("]", "");
//        } else {
//            tags = "null";
//        }
//        if (map.containsKey(JK_CATEGORY)) {
//            category = map.get(JK_CATEGORY).toString();
//        } else {
//            category = "null";
//        }

//        addTags(tags, id);
//        addCategory(category, id);

        long date = 0;

        if (type == 0) {
            int i = id.indexOf('-', 1 + id.indexOf('-', 1 + id.indexOf('-')));
            date = Long.parseLong(id.substring(0, i).replace("-", ""));
        }

        // First, check if the post exists in the db
        Cursor cursorId = mContext.getContentResolver().query(
                PostEntry.CONTENT_URI,
                new String[]{PostEntry.COLUMN_POST_ID},
                PostEntry.COLUMN_POST_ID + " = ?",
                new String[]{id},
                null);

//        Cursor cursorContent = mContext.getContentResolver().query(
//                PostEntry.CONTENT_URI,
//                new String[]{PostEntry.COLUMN_CONTENT},
//                PostEntry.COLUMN_CONTENT + " = ?",
//                new String[]{content},
//                null);

        ContentValues postValues = new ContentValues();

        if (cursorId.moveToFirst()) {
            cursorId.close();
//            if (!cursorContent.moveToFirst()) {
//                cursorContent.close();
                ContentValues updateValues = new ContentValues();
                updateValues.put(PostEntry.COLUMN_CONTENT, postContent);
                if (updateValues.size() > 0) {
                    mContext.getContentResolver().update(PostEntry.CONTENT_URI, updateValues,
                            PostEntry.COLUMN_POST_ID + " = \"" + id + "\"", null);
                }

//            }
        } else {
            cursorId.close();
//            cursorContent.close();

            postValues.put(PostEntry.COLUMN_TITLE, title);
            postValues.put(PostEntry.COLUMN_DATETEXT, date);
            postValues.put(PostEntry.COLUMN_DRAFT, type);
            postValues.put(PostEntry.COLUMN_CONTENT, postContent);
            postValues.put(PostEntry.COLUMN_POST_ID, id);
        }

        return postValues;

    }

}
