package gr.tsagi.jekyllforandroid.github;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by tsagi on 2/2/14.
 */

public interface GitHubService {
    @GET("/users/{user}/repos")
    List<GithubClient.Repo> listRepos(@Path("user") String user);
}
