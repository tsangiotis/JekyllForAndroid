package gr.tsagi.jekyllforandroid.app.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.jchanghong.R;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.UserService;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import gr.tsagi.jekyllforandroid.app.utils.FetchPostsTask;
import gr.tsagi.jekyllforandroid.app.utils.GetAccessToken;
import gr.tsagi.jekyllforandroid.app.utils.JekyllRepo;

//import com.readystatesoftware.systembartint.SystemBarTintManager;

public class LoginActivity extends BaseActivity {
    private static String CLIENT_ID = "1569f7710e0b37bb066c";
    //Use your own client id
    private static String CLIENT_SECRET = "f28ab3c713d44d4cc582c09fa7afe38e5e6024b4";
    //Use your own client secret
    private static String REDIRECT_URI = "http://localhost";
    private static String GRANT_TYPE = "auth_code";
    private static String TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static String OAUTH_URL = "https://github.com/login/oauth/authorize";
    private static String OAUTH_SCOPE = "user%2Crepo";
    //Change the Scope as you need
    WebView web;
    ImageButton auth;
    SharedPreferences settings;
    TextView logview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     logview  = (TextView) findViewById(R.id.log);
        settings = getSharedPreferences(
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);

        auth = (ImageButton) findViewById(R.id.fab);

        auth.setOnClickListener(new View.OnClickListener() {
            Dialog auth_dialog;

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                auth_dialog = new Dialog(LoginActivity.this);
                auth_dialog.setContentView(R.layout.auth_dialog);
                web = (WebView) auth_dialog.findViewById(R.id.webv);
                web.getSettings().setJavaScriptEnabled(true);
                web.loadUrl(OAUTH_URL + "?redirect_uri=" + REDIRECT_URI + "&response_type=code&client_id=" + CLIENT_ID + "&scope=" + OAUTH_SCOPE);
                web.setWebViewClient(new WebViewClient() {
                    boolean authComplete = false;
                    Intent resultIntent = new Intent();

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        super.onPageStarted(view, url, favicon);
                    }

                    String authCode;

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        if (url.contains("?code=") && authComplete != true) {
                            Uri uri = Uri.parse(url);
                            authCode = uri.getQueryParameter("code");
                            Log.i("", "CODE : " + authCode);
                            authComplete = true;
                            resultIntent.putExtra("code", authCode);
                            LoginActivity.this.setResult(Activity.RESULT_OK, resultIntent);
                            setResult(Activity.RESULT_CANCELED, resultIntent);
                            SharedPreferences.Editor edit = settings.edit();
                            edit.putString("Code", authCode);
                            edit.commit();
                            auth_dialog.dismiss();
                            new TokenGet().execute();
                        } else if (url.contains("error=access_denied")) {
                            Log.i("", "ACCESS_DENIED_HERE");
                            resultIntent.putExtra("code", authCode);
                            authComplete = true;
                            setResult(Activity.RESULT_CANCELED, resultIntent);
                            Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_SHORT).show();
                            auth_dialog.dismiss();
                        }
                    }
                });
                auth_dialog.show();
                auth_dialog.setTitle("Authorize Jekyll for Android");
                auth_dialog.setCancelable(true);
            }
        });
    }

    @Override protected int getLayoutResource() {
        return R.layout.activity_login;
    }

    private class TokenGet extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog pDialog;
        String Code;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Contacting Github ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            Code = settings.getString("Code", "");
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            GetAccessToken jParser = new GetAccessToken();
            return jParser.gettoken(TOKEN_URL, Code, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI,
                    GRANT_TYPE);
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            pDialog.dismiss();
            if (json != null) {
                try {
                    String tok = json.getString("access_token");
                    Log.d("Token Access", tok);
                    //TODO: React to touch
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("user_status", tok);
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                        editor.apply();
                    } else {
                        editor.commit();
                    }
                    new UserGet().execute();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Network Error", Toast.LENGTH_SHORT).show();
                pDialog.dismiss();
            }
        }
    }

    private class UserGet extends AsyncTask<Void, Void, Void> {

        String user = "";

        @Override
        protected Void doInBackground(Void... args) {
            GitHubClient client = new GitHubClient();
            client.setOAuth2Token(settings.getString("user_status", ""));
            UserService uService = new UserService(client);
            try {
                User us = uService.getUser();
                user = us.getLogin();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d("LoginUser", settings.getString("user_status", ""));
            JekyllRepo uRepo = new JekyllRepo();
            String repo = uRepo.getName(user);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("user_login", user);
            editor.putString("user_repo", repo);
            editor.commit();

          FetchPostsTask  fetchPostsTask = new FetchPostsTask(LoginActivity.this,logview);
            fetchPostsTask.execute();

//            finish();
        }
    }
}
