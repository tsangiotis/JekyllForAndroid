package gr.tsagi.jekyllforandroid.activities;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.UserService;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.utils.GetAccessToken;
import gr.tsagi.jekyllforandroid.utils.JekyllRepo;

public class LoginActivity extends Activity {
    private static String CLIENT_ID = "c93bd14e3c9671bd7dbf";
    //Use your own client id
    private static String CLIENT_SECRET = "e1566daf8025707db0d5fefa146b965f3ad38086";
    //Use your own client secret
    private static String REDIRECT_URI = "http://localhost";
    private static String GRANT_TYPE = "auth_code";
    private static String TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static String OAUTH_URL = "https://github.com/login/oauth/authorize";
    private static String OAUTH_SCOPE = "user%2Crepo";
    //Change the Scope as you need
    WebView web;
    Button auth;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        settings = getSharedPreferences(
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);
        auth = (Button) findViewById(R.id.auth);
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
                    auth.setText("Authenticated");
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("user_status", tok);
                    editor.apply();
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

        String Token = "";
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
            finish();
        }
    }
}
