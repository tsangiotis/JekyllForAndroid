package gr.tsagi.jekyllforandroid;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import gr.tsagi.jekyllforandroid.utils.JekyllRepo;
import gr.tsagi.roboelectric.RobolectricGradleTestRunner;

import static junit.framework.Assert.assertEquals;

@Config(emulateSdk = 18)
@RunWith(RobolectricGradleTestRunner.class)
public class JekyllRepoTest {

    private JekyllRepo repo;

    @Before
    public void setup() {
        repo = new JekyllRepo();
    }

    @Test
    public void getsCorrectRepo() {
        assertEquals(repo.getName("tsagi"), "tsagi.github.com");
    }
}