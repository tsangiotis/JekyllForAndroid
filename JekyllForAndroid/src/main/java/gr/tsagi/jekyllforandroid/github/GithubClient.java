package gr.tsagi.jekyllforandroid.github;

/**
 * Created by tsagi on 2/2/14.
 */

public class GithubClient {
    private static final String API_URL = "https://api.github.com";

    static class Repo {
        public final String name;

        Repo(String name) {
            this.name = name;
        }
    }
}
