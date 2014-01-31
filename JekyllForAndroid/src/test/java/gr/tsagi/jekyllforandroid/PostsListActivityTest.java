package gr.tsagi.jekyllforandroid;

import android.widget.ListView;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import gr.tsagi.jekyllforandroid.activities.PostsListActivity;
import gr.tsagi.roboelectric.RobolectricGradleTestRunner;

import static org.fest.assertions.api.ANDROID.assertThat;

/**
 * Created by tsagi on 1/30/14.
 */

@Config(emulateSdk = 18)
@RunWith(RobolectricGradleTestRunner.class)
public class PostsListActivityTest {

    private PostsListActivity activity;

    @Before
    public void setup() {
        activity = Robolectric.buildActivity(PostsListActivity.class).create().get();
    }

    @Test
    public void shouldNotBeNull() {
        assertThat(activity).isNotNull();

        // Main Elements
        ListView postslist = (ListView) activity.findViewById(R.id.posts_list);
        assertThat(postslist).isNotNull();

        // Loading Status
        TextView statusText = (TextView) activity.findViewById(R.id.posts_list_status_message);
        assertThat(statusText).isNotNull();
    }

}
