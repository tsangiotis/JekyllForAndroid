package gr.tsagi.jekyllforandroid.app.utils

import android.content.ContentValues
import android.content.Context
import android.util.Base64
import com.jchanghong.GlobalApplication
import com.jchanghong.model.Category
import com.jchanghong.model.Note
import gr.tsagi.jekyllforandroid.app.data.PostsContract.PostEntry
import org.yaml.snakeyaml.Yaml
import java.io.*


/**
 * Created by tsagi on 8/15/14.
 */
class ParsePostData(private val mContext: Context) {

    private val LOG_TAG = ParsePostData::class.java.simpleName


    internal val JK_TITLE = "title"
    internal val JK_CATEGORY = "category"
    internal val JK_TAGS = "tags"


    fun getDataFromContent(id: String, contentBytes: String, type: Int): ContentValues {

        // Get and insert the new posts information into the database
        var postContent: String? = null

        // Blobs return with Base64 encoding so we have to UTF-8 them.
        val bytes = Base64.decode(contentBytes, Base64.DEFAULT)
        try {
            postContent = String(bytes, charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        val stringBuilder = StringBuilder()

        val `is`: InputStream
        val r: BufferedReader

        `is` = ByteArrayInputStream(postContent!!.toByteArray())
        // read it with BufferedReader
        r = BufferedReader(InputStreamReader(`is`))
        var line: String?

        var yaml_dash = 0
        var yamlStr = ""
        try {
            while (true) {
                line = r.readLine()
                if (line == null) {
                    break
                }
                if (line == "---") {
                    yaml_dash++
                }
                if (yaml_dash < 2) {
                    if (line != "---")
                        yamlStr = yamlStr + line + "\n"
                }
                if (yaml_dash >= 2) {
                    if (line != "---")
                        if (line == "")
                            stringBuilder.append("\n")
                        else
                            stringBuilder.append(line)
                }
            }
            `is`.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }


        val content = stringBuilder.toString()


        val yaml = Yaml()

        val map: Map<*, *>
        map = yaml.load(yamlStr) as Map<*, *>

        var title = id
        var tags: String? = null
        var category: String? = null

        title = map[JK_TITLE].toString()

        if (map.containsKey(JK_TAGS)) {
            tags = map[JK_TAGS].toString().replace("[", "").replace("]", "")
        } else {
            tags = null
        }
        if (map.containsKey(JK_CATEGORY)) {
            category = map[JK_CATEGORY].toString()
        } else {
            category = null
        }
        val categorymode = getornew(tags, category)

        var anote = Note()
        anote.category=categorymode
        anote.tittle=title
        anote.content=postContent
        manager.insertNote(anote)

        //        addTags(tags, id);
        //        addCategory(category, id);

        var date: Long = 0

        if (type == 0) {
            val i = id.indexOf('-', 1 + id.indexOf('-', 1 + id.indexOf('-')))
            date = java.lang.Long.parseLong(id.substring(0, i).replace("-", ""))
        }
        anote.lastEdit=date

        System.out.println(anote)
        // First, check if the post exists in the db
        val cursorId = mContext.contentResolver.query(
                PostEntry.CONTENT_URI,
                arrayOf(PostEntry.COLUMN_POST_ID),
                PostEntry.COLUMN_POST_ID + " = ?",
                arrayOf(id), null)

        val cursorContent = mContext.contentResolver.query(
                PostEntry.CONTENT_URI,
                arrayOf(PostEntry.COLUMN_CONTENT),
                PostEntry.COLUMN_CONTENT + " = ?",
                arrayOf(content), null)

        val postValues = ContentValues()

        if (cursorId!!.moveToFirst()) {
            cursorId.close()
            if (!cursorContent!!.moveToFirst()) {
                cursorContent.close()
                val updateValues = ContentValues()
                updateValues.put(PostEntry.COLUMN_CONTENT, postContent)
                if (updateValues.size() > 0) {
                    mContext.contentResolver.update(PostEntry.CONTENT_URI, updateValues,
                            PostEntry.COLUMN_POST_ID + " = \"" + id + "\"", null)
                }

            }
        } else {
            cursorId.close()
            cursorContent!!.close()

            postValues.put(PostEntry.COLUMN_TITLE, title)
            postValues.put(PostEntry.COLUMN_DATETEXT, date)
            postValues.put(PostEntry.COLUMN_DRAFT, type)
            postValues.put(PostEntry.COLUMN_CONTENT, postContent)
            postValues.put(PostEntry.COLUMN_POST_ID, id)
        }

        return postValues

    }

    private val manager = GlobalApplication.db
    private fun getornew(tags: String?, category: String?): Category {
        if (category != null) {
            val c = Category(name = category)
            if (c.exit()) {
                return c

            }
            else{
                c.create()
                return c
            }
        }
        if (tags != null) {
           var c=Category(name = tags.trim().split(" ")[0])
            if (c.exit()) {
                return c

            }
            else{
                c.create()
                return c
            }
        }
        return manager.firstCategory

    }

}
