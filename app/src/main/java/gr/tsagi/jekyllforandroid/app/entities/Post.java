package gr.tsagi.jekyllforandroid.app.entities;

import java.util.Date;

/**
 * Created by tsagi on 10/21/14.
 */
public class Post {
    public String title;
    public Date date;
    public String content;
    public String category;
    public String tags;

    public void setTitle(String postTitle){
        title = postTitle;
    }

    public void setDate(Date postDate){
        date = postDate;
    }

    public void setContent(String postContent){
        content = postContent;
    }

    public void setCategory(String postCategory){
        category = postCategory;
    }

    public void setTags(String postTags){
        tags = postTags;
    }
}
