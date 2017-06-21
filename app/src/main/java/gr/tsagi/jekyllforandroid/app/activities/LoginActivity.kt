package gr.tsagi.jekyllforandroid.app.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.jchanghong.R
import gr.tsagi.jekyllforandroid.app.utils.FetchPostsTask
import gr.tsagi.jekyllforandroid.app.utils.GetAccessToken
import gr.tsagi.jekyllforandroid.app.utils.JekyllRepo
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.UserService
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


class LoginActivity : BaseActivity() {
    //Change the Scope as you need
    lateinit internal var web: WebView
    lateinit internal var auth: ImageButton
    lateinit internal var settings: SharedPreferences
    lateinit internal var logview: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logview = findViewById(R.id.log) as TextView
        logview.text = "login......."
        settings = getSharedPreferences(
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE)

        auth = findViewById(R.id.fab) as ImageButton

        auth.setOnClickListener(object : View.OnClickListener {
            lateinit internal var auth_dialog: Dialog

            override fun onClick(arg0: View) {
                // TODO Auto-generated method stub
                auth_dialog = Dialog(this@LoginActivity)
                auth_dialog.setContentView(R.layout.auth_dialog)
                web = auth_dialog.findViewById<View>(R.id.webv) as WebView
                web.settings.javaScriptEnabled = true
                web.loadUrl("$OAUTH_URL?redirect_uri=$REDIRECT_URI&response_type=code&client_id=$CLIENT_ID&scope=$OAUTH_SCOPE")
                web.webViewClient = object : WebViewClient() {
                    internal var authComplete = false
                    internal var resultIntent = Intent()

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
//                        super.onPageStarted(view, url, favicon)
                    }

                    lateinit internal var authCode: String

                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)
                        if (url.contains("?code=") && authComplete != true) {
                            val uri = Uri.parse(url)
                            authCode = uri.getQueryParameter("code")
                            Log.i("", "CODE : " + authCode)
                            authComplete = true
                            resultIntent.putExtra("code", authCode)
                            this@LoginActivity.setResult(Activity.RESULT_OK, resultIntent)
                            setResult(Activity.RESULT_CANCELED, resultIntent)
                            val edit = settings.edit()
                            edit.putString("Code", authCode)
                            edit.commit()
                            auth_dialog.dismiss()
                            TokenGet().execute()
                        } else if (url.contains("error=access_denied")) {
                            Log.i("", "ACCESS_DENIED_HERE")
                            resultIntent.putExtra("code", authCode)
                            authComplete = true
                            setResult(Activity.RESULT_CANCELED, resultIntent)
                            Toast.makeText(applicationContext, "Error Occured", Toast.LENGTH_SHORT).show()
                            auth_dialog.dismiss()
                        }
                    }
                }
                auth_dialog.show()
                auth_dialog.setTitle("Authorize Jekyll for Android")
                auth_dialog.setCancelable(true)
            }
        })
    }

    override val layoutResource: Int
        get() = R.layout.activity_login

    private inner class TokenGet : AsyncTask<String, String, JSONObject>() {
        private var pDialog: ProgressDialog? = null
        lateinit internal var Code: String

        override fun onPreExecute() {
            super.onPreExecute()
            pDialog = ProgressDialog(this@LoginActivity)
            pDialog!!.setMessage("Contacting Github ...")
            pDialog!!.isIndeterminate = false
            pDialog!!.setCancelable(true)
            Code = settings.getString("Code", "")
            pDialog!!.show()
        }

        override fun doInBackground(vararg args: String): JSONObject {
            val jParser = GetAccessToken()
            return jParser.gettoken(TOKEN_URL, Code, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI,
                    GRANT_TYPE)
        }

        @SuppressLint("ObsoleteSdkInt")
        override fun onPostExecute(json: JSONObject?) {
            pDialog!!.dismiss()
            if (json != null) {
                try {
                    val tok = json.getString("access_token")
                    Log.d("Token Access", tok)
                    //TODO: React to touch
                    val editor = settings.edit()
                    editor.putString("user_status", tok)
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                        editor.apply()
                    } else {
                        editor.commit()
                    }
                    UserGet().execute()
                } catch (e: JSONException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }

            } else {
                Toast.makeText(applicationContext, "Network Error", Toast.LENGTH_SHORT).show()
                pDialog!!.dismiss()
            }
        }
    }

    private inner class UserGet : AsyncTask<Void, Void, Void>() {

        internal var user = ""

        override fun doInBackground(vararg args: Void): Void? {
            val client = GitHubClient()
            client.setOAuth2Token(settings.getString("user_status", ""))
            val uService = UserService(client)
            try {
                val us = uService.user
                user = us.login
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }


        override fun onPostExecute(aVoid: Void?) {
            Log.d("LoginUser", settings.getString("user_status", ""))
            val uRepo = JekyllRepo()
            val repo = uRepo.getName(user)
            val editor = settings.edit()
            editor.putString("user_login", user)
            editor.putString("user_repo", repo)
            editor.commit()
            val fetchPostsTask = FetchPostsTask(this@LoginActivity, logview)
            fetchPostsTask.execute()
            //            finish();
        }
    }


    companion object {
        private val CLIENT_ID = "1569f7710e0b37bb066c"
        //Use your own client id
        private val CLIENT_SECRET = "f28ab3c713d44d4cc582c09fa7afe38e5e6024b4"
        //Use your own client secret
        private val REDIRECT_URI = "http://localhost"
        private val GRANT_TYPE = "auth_code"
        private val TOKEN_URL = "https://github.com/login/oauth/access_token"
        private val OAUTH_URL = "https://github.com/login/oauth/authorize"
        private val OAUTH_SCOPE = "user%2Crepo"
    }
}
