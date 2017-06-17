package gr.tsagi.jekyllforandroid.app.utils

import android.content.ContentValues
import android.content.Context
import android.util.Base64
import gr.tsagi.jekyllforandroid.app.data.PostsContract.PostEntry
import java.io.UnsupportedEncodingException


/**
\* Created with IntelliJ IDEA.
\* User: tsagi
\* Date: 8/15/14
\* Time: 9:15
\*/
class ParsePostData(private val mContext: Context) {


    internal val JK_TITLE = "title"
    internal val JK_CATEGORY = "category"
    internal val JK_TAGS = "tags"


    fun getDataFromContent(id: String, contentBytes: String, type: Int): ContentValues {

        // Get and insert the new posts information into the database
        var postContent: String? =
                null

        // Blobs return with Base64 encoding so we have to UTF-8 them.
        val bytes = Base64.decode(contentBytes, Base64.DEFAULT)
        try {
            postContent = String(bytes, charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
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

        val title = id
        val tags = "null"
        val category = "null"

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

        var date: Long = 0

        if (type == 0) {
            val i = id.indexOf('-', 1 + id.indexOf('-', 1 + id.indexOf('-')))
            date = java.lang.Long.parseLong(id.substring(0, i).replace("-", ""))
        }

        // First, check if the post exists in the db
        val cursorId = mContext.contentResolver.query(
                PostEntry.CONTENT_URI,
                arrayOf(PostEntry.COLUMN_POST_ID),
                PostEntry.COLUMN_POST_ID + " = ?",
                arrayOf(id), null)

        //        Cursor cursorContent = mContext.getContentResolver().query(
        //                PostEntry.CONTENT_URI,
        //                new String[]{PostEntry.COLUMN_CONTENT},
        //                PostEntry.COLUMN_CONTENT + " = ?",
        //                new String[]{content},
        //                null);

        val postValues = ContentValues()

        if (cursorId!!.moveToFirst()) {
            cursorId.close()
            //            if (!cursorContent.moveToFirst()) {
            //                cursorContent.close();
            val updateValues = ContentValues()
            updateValues.put(PostEntry.COLUMN_CONTENT, postContent)
            if (updateValues.size() > 0) {
                mContext.contentResolver.update(PostEntry.CONTENT_URI, updateValues,
                        PostEntry.COLUMN_POST_ID + " = \"" + id + "\"", null)
            }

            //            }
        } else {
            cursorId.close()
            //            cursorContent.close();

            postValues.put(PostEntry.COLUMN_TITLE, title)
            postValues.put(PostEntry.COLUMN_DATETEXT, date)
            postValues.put(PostEntry.COLUMN_DRAFT, type)
            postValues.put(PostEntry.COLUMN_CONTENT, postContent)
            postValues.put(PostEntry.COLUMN_POST_ID, id)
        }

        return postValues

    }

}
