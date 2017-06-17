package gr.tsagi.jekyllforandroid.app.utils

import android.util.Log
import org.apache.http.NameValuePair
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.util.*

/**
\* Created with IntelliJ IDEA.
\* User: tsagi
\* Date: 7/7/14
\* Time: 9:15
\*/

class GetAccessToken {
    internal var jsondel = ""
    internal var params: MutableList<NameValuePair> = ArrayList()
    internal var mapn: Map<String, String>? = null
 lateinit   internal var httpClient: DefaultHttpClient
   lateinit internal var httpPost: HttpPost
    fun gettoken(address: String, token: String, client_id: String, client_secret: String, redirect_uri: String, grant_type: String): JSONObject {
        // Making HTTP request
        try {
            // DefaultHttpClient
            httpClient = DefaultHttpClient()
            httpPost = HttpPost(address)
            params.add(BasicNameValuePair("code", token))
            params.add(BasicNameValuePair("client_id", client_id))
            params.add(BasicNameValuePair("client_secret", client_secret))
            params.add(BasicNameValuePair("redirect_uri", redirect_uri))
            params.add(BasicNameValuePair("grant_type", grant_type))
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded")
            httpPost.entity = UrlEncodedFormEntity(params)
            val httpResponse = httpClient.execute(httpPost)
            val httpEntity = httpResponse.entity
            `is` = httpEntity.content
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: ClientProtocolException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        try {
            val reader = BufferedReader(InputStreamReader(
                    `is`!!, "iso-8859-1"), 8)
            val sb = StringBuilder()
            var line: String? = reader.readLine()
            do {
                sb.append(line!! + "n")
                line=reader.readLine()
            }while (line!=null)
//            while ((line = reader.readLine()) != null) {
//
//            }
            `is`!!.close()
            json = sb.toString()
            Log.e("JSONStr", json)
            jsondel = json.replace("&", "\",\"").replace("=", "\"=\"")
            Log.e("JSONStrDel", jsondel)

        } catch (e: Exception) {
            e.message
            Log.e("Buffer Error", "Error converting result " + e.toString())
        }

        // Parse the String to a JSON Object
        try {
            jObj = JSONObject("{\"$jsondel\"}")
        } catch (e: JSONException) {
            Log.e("JSON Parser", "Error parsing data " + e.toString())
        }

        // Return JSON String
        return jObj!!
    }

    companion object {
        internal var `is`: InputStream? = null
        internal var jObj: JSONObject? = null
        internal var json = ""
    }
}