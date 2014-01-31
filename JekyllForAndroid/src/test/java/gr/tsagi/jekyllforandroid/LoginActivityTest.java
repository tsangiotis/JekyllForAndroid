package gr.tsagi.jekyllforandroid;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import gr.tsagi.jekyllforandroid.activities.LoginActivity;
import gr.tsagi.roboelectric.RobolectricGradleTestRunner;

import static org.fest.assertions.api.ANDROID.assertThat;

/**
 * Created by tsagi on 1/30/14.
 */

@Config(emulateSdk = 18)
@RunWith(RobolectricGradleTestRunner.class)
public class LoginActivityTest {

    private LoginActivity activity;

    @Before
    public void setup() {
        activity = Robolectric.buildActivity(LoginActivity.class).create().get();
    }

    @Test
    public void shouldNotBeNull() {
        assertThat(activity).isNotNull();

        // Main Elements
        EditText username= (EditText) activity.findViewById(R.id.username);
        assertThat(username).isNotNull();

        EditText password= (EditText) activity.findViewById(R.id.password);
        assertThat(password).isNotNull();

        Button signInButton = (Button) activity.findViewById(R.id.sign_in_button);
        assertThat(signInButton).isNotNull();

        // Jekyll not found elements
        TextView infoText = (TextView) activity.findViewById(R.id.jinfotextView);
        assertThat(infoText).isNotNull();

        Button infoButton = (Button) activity.findViewById(R.id.jinfoButton);
        assertThat(infoButton).isNotNull();

        Spinner infoSpinner = (Spinner) activity.findViewById(R.id.jinfospinner);
        assertThat(infoSpinner).isNotNull();

        // Login Status
        TextView statusText = (TextView) activity.findViewById(R.id.login_status_message);
        assertThat(statusText).isNotNull();
    }


}
