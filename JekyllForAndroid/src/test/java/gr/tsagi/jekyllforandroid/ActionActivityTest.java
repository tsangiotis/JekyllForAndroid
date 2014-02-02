package gr.tsagi.jekyllforandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.MenuItem;
import android.widget.ImageView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.tester.android.view.TestMenuItem;

import gr.tsagi.jekyllforandroid.activities.ActionActivity;
import gr.tsagi.jekyllforandroid.activities.EditPostActivity;
import gr.tsagi.jekyllforandroid.activities.LoginActivity;
import gr.tsagi.jekyllforandroid.activities.PostsListActivity;
import gr.tsagi.jekyllforandroid.activities.SetPreferenceActivity;
import gr.tsagi.roboelectric.RobolectricGradleTestRunner;

import static junit.framework.Assert.assertEquals;
import static org.fest.assertions.api.ANDROID.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricGradleTestRunner.class)
public class ActionActivityTest {

    private ActionActivity activity;
    SharedPreferences sharedPreferences = Robolectric.application.
            getSharedPreferences("gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);

    @Before
    public void setup() {

        sharedPreferences.edit().putString("user_status", "ok").commit();
        activity = Robolectric.buildActivity(ActionActivity.class).create().get();
    }

    @Test
    public void shouldNotBeNull() {
        assertThat(activity).isNotNull();

        // Main Elements
        ImageView imageView = (ImageView) activity.findViewById(R.id.imageView);
        assertThat(imageView);

        //Menu Elements
        MenuItem newPost = new TestMenuItem(R.id.action_new);
        assertThat(newPost).isNotNull();

        MenuItem postsList = new TestMenuItem(R.id.action_list);
        assertThat(postsList).isNotNull();

        MenuItem logout = new TestMenuItem(R.id.action_logout);
        assertThat(logout).isNotNull();

        MenuItem settings = new TestMenuItem(R.id.action_settings);
        assertThat(settings).isNotNull();
    }

    @Test
    public void postsListToPostsListActivity() throws Exception {

        MenuItem postsList = new TestMenuItem(R.id.action_list);
        activity.onOptionsItemSelected(postsList);
        ShadowActivity shadowActivity = Robolectric.shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Robolectric.shadowOf(startedIntent);

        assertEquals(PostsListActivity.class.getName(), shadowIntent.getComponent().getClassName());
    }

    @Test
    public void newPostToNewPostActivity() throws Exception {

        MenuItem newPost = new TestMenuItem(R.id.action_new);
        activity.onOptionsItemSelected(newPost);
        ShadowActivity shadowActivity = Robolectric.shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Robolectric.shadowOf(startedIntent);

        assertEquals(EditPostActivity.class.getName(), shadowIntent.getComponent().getClassName());
    }

    @Test
    public void settingsToSettingsActivity() throws Exception {

        MenuItem settings = new TestMenuItem(R.id.settings);
        activity.onOptionsItemSelected(settings);
        ShadowActivity shadowActivity = Robolectric.shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Robolectric.shadowOf(startedIntent);

        assertEquals(SetPreferenceActivity.class.getName(), shadowIntent.getComponent().getClassName());
    }

    @Test
    public void logoutToLogoutActivity() throws Exception {

        sharedPreferences.getString("user_status", "");

        MenuItem logout = new TestMenuItem(R.id.action_logout);
        activity.onOptionsItemSelected(logout);
        dialogAction(1);
        ShadowActivity shadowActivity = Robolectric.shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Robolectric.shadowOf(startedIntent);

        assertEquals(LoginActivity.class.getName(), shadowIntent.getComponent().getClassName());
    }

    @Test
    public void eraseUserData() throws Exception {

        sharedPreferences.getString("user_status", "");

        MenuItem logout = new TestMenuItem(R.id.action_logout);
        activity.onOptionsItemSelected(logout);
        AlertDialog alertDialog = null;
        dialogAction(1);

        // check if userinfo is cleared deleted
        assertEquals("", sharedPreferences.getString("user_status", ""));
        assertEquals("", sharedPreferences.getString("user_login", ""));
        assertEquals("", sharedPreferences.getString("user_repo", ""));
        assertEquals("", sharedPreferences.getString("draft_title", ""));
        assertEquals("", sharedPreferences.getString("draft_category", ""));
        assertEquals("", sharedPreferences.getString("draft_tags", ""));
        assertEquals("", sharedPreferences.getString("draft_content", ""));
        assertEquals("", sharedPreferences.getString("yaml_values", ""));
        assertEquals("", sharedPreferences.getString("other_values", ""));
        assertEquals("", sharedPreferences.getString("posts_subdir", ""));
    }

    public void dialogAction(int choice) {
        AlertDialog alertDialog = null;
        if(ShadowAlertDialog.getLatestAlertDialog() != null){
            alertDialog = ShadowAlertDialog.getLatestAlertDialog();
            switch (choice){
                case 0:
                    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
                    break;
                default:
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
            }

        }
    }
}