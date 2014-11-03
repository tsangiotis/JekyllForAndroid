package gr.tsagi.jekyllforandroid.app.entities;

/**
 * Created by tsagi on 10/21/14.
 */
public class Post {
    public String id;
    public String title;
    public int draft;
    public String date;
    public String content;
    public String category;
    public String tags;

    public void setTitle(String postTitle){
        title = postTitle;
    }

    public void setDate(String postDate){
        date = postDate;
    }

    public void isDraft() {
        draft = 1;
    }

    public void noDraft() {
        draft = 0;
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

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public int getStatus() {
        return draft;
    }

    public String getContent() {
        return content;
    }

    public String getCategory() {
        return category;
    }

    public String getTags() {
        return tags;
    }
}
