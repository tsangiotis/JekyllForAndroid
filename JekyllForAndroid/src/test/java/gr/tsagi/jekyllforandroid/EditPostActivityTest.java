package gr.tsagi.jekyllforandroid;

import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.tester.android.view.TestMenuItem;

import gr.tsagi.jekyllforandroid.activities.EditPostActivity;
import gr.tsagi.roboelectric.RobolectricGradleTestRunner;

import static org.fest.assertions.api.ANDROID.assertThat;

/**
 * Created by tsagi on 1/30/14.
 */

@Config(emulateSdk = 18)
@RunWith(RobolectricGradleTestRunner.class)
public class EditPostActivityTest {

    private EditPostActivity activity;

    @Before
    public void setup() {
        activity = Robolectric.buildActivity(EditPostActivity.class).create().get();
    }

    @Test
    public void shouldNotBeNull() {
        assertThat(activity).isNotNull();

        // Main Elements
        EditText title= (EditText) activity.findViewById(R.id.editTextTitle);
        assertThat(title).isNotNull();

        EditText category= (EditText) activity.findViewById(R.id.editTextCategory);
        assertThat(category).isNotNull();

        EditText tags= (EditText) activity.findViewById(R.id.editTextTags);
        assertThat(tags).isNotNull();

        EditText content= (EditText) activity.findViewById(R.id.editTextContent);
        assertThat(content).isNotNull();

        //Menu Elements
        MenuItem publish = new TestMenuItem(R.id.action_publish);
        assertThat(publish).isNotNull();

        MenuItem preview = new TestMenuItem(R.id.action_preview);
        assertThat(preview).isNotNull();

        MenuItem clear = new TestMenuItem(R.id.action_clear_draft);
        assertThat(clear).isNotNull();

        MenuItem settings = new TestMenuItem(R.id.action_settings);
        assertThat(settings).isNotNull();

        // Loading Status
        TextView statusText = (TextView) activity.findViewById(R.id.newpost_status_message);
        assertThat(statusText).isNotNull();
    }
}
