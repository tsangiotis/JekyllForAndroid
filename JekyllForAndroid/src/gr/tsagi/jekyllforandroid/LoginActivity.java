package gr.tsagi.jekyllforandroid;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.egit.github.core.Authorization;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.OAuthService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private LoginTask mAuthTask = null;

    // Values for email and password at the time of the login attempt.
    private String mUsername;
    private String mPassword;
    
    // Arrays for Jekyll info
	private String[] jurls;

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        jurls = this.getResources().getStringArray(R.array.gpresourceslinks);

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);
        mUsernameView.setText(mUsername);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                	hideSoftKeyboard(LoginActivity.this);
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
        
        // Hide the keyboard on login attempt
        final Button loginBtn = (Button)findViewById(R.id.sign_in_button);
         
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard(LoginActivity.this);
                attemptLogin();
            }
        });

        final Button goBtn = (Button)findViewById(R.id.jinfoButton);
    	final Spinner spinner = (Spinner)findViewById(R.id.jinfospinner);
        
        goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(jurls[spinner.getSelectedItemPosition()]));
            	startActivity(browserIntent);
            }
        });
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login_forgot_password:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
         
        mUsername = mUsernameView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (mPassword.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(mUsername)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // perform the user login attempt.
            mAuthTask = new LoginTask(LoginActivity.this);
            mAuthTask.execute();
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
   

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private class LoginTask extends AsyncTask<Void, Void, Authorization> {

        /**
         * The target.
         */
        private WeakReference<LoginActivity> mTarget;
        /**
         * The exception.
         */
        private boolean mException;
        /**
         * The is auth error.
         */
        
        private boolean jException;
        
        private boolean isAuthError;
        /**
         * Instantiates a new load repository list task.
         *
         * @param activity the activity
         */
        public LoginTask(LoginActivity activity) {
            mTarget = new WeakReference<LoginActivity>(activity);
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected Authorization doInBackground(Void... params) {
            if (mTarget.get() != null) {
                try {
                    /**
                     * Get username and password from TextEdits
                     */
                    String username = mTarget.get().mUsernameView.getText().toString();
                    String password = mTarget.get().mPasswordView.getText().toString();

                    /**
                     * Initiate GitHub client
                     */
                    GitHubClient client = new GitHubClient();
                    client.setCredentials(username,
                            password);
                    client.setUserAgent("Jekyll for Android");
                    RepositoryService repositoryService = new RepositoryService();
                    
                    
                    Repository repository =  repositoryService.getRepository(mUsername, mUsername +".github.com");
                    
                    
                    /**
                     * Get OAuth if there isn't one
                     */
                    Authorization auth = null;
                    OAuthService authService = new OAuthService(client);
                    List<Authorization> auths = authService.getAuthorizations();
                    for (Authorization authorization : auths) {
                        if ("Jekyll for Android".equals(authorization.getNote())) {
                            auth = authorization;
                            break;
                        }
                    }

                    if (auth == null) {
                        auth = new Authorization();
                        auth.setNote("Jekyll for Android");
                        auth.setUrl("http://tsagi.me");
                        List<String> scopes = new ArrayList<String>();
                        scopes.add("user");
                        scopes.add("repo");
                        auth.setScopes(scopes);

                        /**
                         * Create OAuth
                         */
                        auth = authService.createAuthorization(auth);
                    }
                    return auth;
                } catch (RequestException re){
                	jException = true;
                	return null;
                	
                }catch (IOException e) {
                    Log.e("EX Tag", e.getMessage(), e);
                    if (e.getMessage().equalsIgnoreCase(
                            "Received authentication challenge is null")) {
                        isAuthError = true;
                    }
                    // Get exception message
                    mException = true;
                    e.getMessage();
                    if (e.getCause() != null) {
                        e.getCause().getMessage();
                    }
                } 
                return null;
            } else {
                return null;
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            /**
             * Start progress view
             */
            if (mTarget.get() != null) {
                mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
                showProgress(true);
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */

        @Override
        protected void onCancelled() {
            /**
             * Stop progress view
             */
            mAuthTask = null;
            showProgress(false);
        }

        @Override
        protected void onPostExecute(Authorization result) {
            /**
             * Stop progress view and Toast the result of the login attempt
             */
        	
            showProgress(false);
            mAuthTask = null;
            if (mTarget.get() != null) {
                LoginActivity activity = mTarget.get();
                if (mException && isAuthError) {
                    Toast.makeText(activity,
                            "Invalid Login",
                            Toast.LENGTH_SHORT).show();
                } else if (jException) {
                	//Toast and set Jekyll info Visible
                    Toast.makeText(activity, "You don't have a Jekyll blog hosted at Github.com", Toast.LENGTH_LONG).show();
                    findViewById(R.id.nojekyll).setVisibility(View.VISIBLE);
                }else if (mException) {
                    Toast.makeText(activity, "Invalid Credentials", Toast.LENGTH_LONG).show();
                }else {
                    SharedPreferences sharedPreferences = getSharedPreferences(
                            "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("user_status", result.getToken());
                    editor.putString("user_login", mTarget.get().mUsernameView.getText().toString());
                    editor.commit();
                    Toast.makeText(activity,
                            "Login succesfull!",
                            Toast.LENGTH_SHORT).show();
                    activity.finish();

                }
            }
            
        }
    }
}
