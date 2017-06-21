package gr.tsagi.jekyllforandroid.app.utils

import android.util.Base64
import android.util.Log
import com.jchanghong.data.DatabaseManager
import com.jchanghong.model.Category
import com.jchanghong.model.Note
import com.jchanghong.utils.date_id_toTitle
import com.jchanghong.utils.getyam
import com.jchanghong.utils.path2Catogery
import org.yaml.snakeyaml.Yaml
import java.io.UnsupportedEncodingException


/**
\* Created with IntelliJ IDEA.
\* User: jchanghong
\* Date: 8/15/14
\* Time: 15:14
\*/
class ParsePostData {

    internal val JK_TITLE = "title"
    internal val JK_CATEGORY = "category"
    internal val JK_TAGS = "tags"

    fun getNoteFrombyte(id: String, path: String, contentBytes: String, type: Int): Note {

        // Get and insert the new posts information into the database
        val note = Note()
        // Blobs return with Base64 encoding so we have to UTF-8 them.
        val bytes = Base64.decode(contentBytes, Base64.DEFAULT)
        val postContent: String =
                try {
                    String(bytes, charset("UTF-8"))
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                    ""
                }
        note.content = postContent

//        val stringBuilder = StringBuilder()
//
//        val inputStream: InputStream
//        val r: BufferedReader
//
//        inputStream = ByteArrayInputStream(postContent?.toByteArray())
//        // read it with BufferedReader
//        r = BufferedReader(InputStreamReader(inputStream))
//        var line: String?
//
//        var yaml_dash = 0
//        var yamlStr = ""
//        try {
//            while (true) {
//                line = r.readLine()
//                if (line == null) {
//                    break
//                }
//                if (line == "---") {
//                    yaml_dash++
//                }
//                if (yaml_dash < 2) {
//                    if (line != "---")
//                        yamlStr = yamlStr + line + "\n"
//                }
//                if (yaml_dash >= 2) {
//                    if (line != "---")
//                        if (line == "")
//                            stringBuilder.append("\n")
//                        else
//                            stringBuilder.append(line)
//                }
//            }
//            inputStream.close()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//
//
//        val content = stringBuilder.toString()
//

        val yaml = Yaml()
        val map: Map<*, *>?
        map = yaml.load(postContent.getyam()) as? Map<*, *>
        var title:String
        var tags: String? = null
        val category: String?

        if (map == null) {
            note.tittle = id.date_id_toTitle()
            var date: Long = 0
            if (type == 0) {
                val i = id.indexOf('-', 1 + id.indexOf('-', 1 + id.indexOf('-')))
                date = java.lang.Long.parseLong(id.substring(0, i).replace("-", ""))
            }
            note.lastEdit = date
            note.category = getornew(null, path.path2Catogery())
            return note
        }
        title = map[JK_TITLE].toString()

        note.tittle = title
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
        note.category = categorymode

        var date: Long = 0

        if (type == 0) {
            val i = id.indexOf('-', 1 + id.indexOf('-', 1 + id.indexOf('-')))
            date = java.lang.Long.parseLong(id.substring(0, i).replace("-", ""))
        }
        note.lastEdit = date
        // First, check if the post exists in the db
        return note
    }

    private fun getornew(tags: String?, category: String?): Category {
        if (category != null) {
            var c = DatabaseManager.getCategoryByName(category)

            if (c != null) {
                return c
            } else {
                c = Category(name = category)
                DatabaseManager.insertCategory(c)
                return c
            }
        }
        if (tags != null) {
            val c1 = Category(name = tags.trim().split(" ")[0])
            val c = DatabaseManager.getCategoryByName(c1.name)

            if (c != null) {
                return c
            } else {
                DatabaseManager.insertCategory(c1)
                return c1
            }
        }
        Log.d("jiangchanghong", "no cat find ,use default")
        return DatabaseManager.defaultCAT
    }

}
