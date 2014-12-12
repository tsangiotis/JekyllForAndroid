package gr.tsagi.jekyllforandroid.app.jfa.model;

import gr.tsagi.jekyllforandroid.app.util.HashUtils;

/**
 * Created by tsagi on 10/21/14.
 */
public class Post {
    public String id;
    public String title;
    public String description;
    public String date;
    public String content;
    public String[] categories;
    public String[] tags;

    public String getImportHashCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("id").append(id == null ? "" : id)
                .append("description").append(description == null ? "" : description)
                .append("title").append(title == null ? "" : title)
                .append("date").append(date == null ? "" : date)
                .append("content").append(content == null ? "" : content);
        for (String tag : tags) {
            sb.append("tag").append(tag);
        }
        for (String category : categories) {
            sb.append("category").append(category);
        }
        return HashUtils.computeWeakHash(sb.toString());
    }


    public String makeTagsList() {
        int i;
        if (tags.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(tags[0]);
        for (i = 1; i < tags.length; i++) {
            sb.append(",").append(tags[i]);
        }
        return sb.toString();
    }

    public boolean hasTag(String tag) {
        for (String myTag : tags) {
            if (myTag.equals(tag)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasCategory(String category) {
        for (String myCategory : categories) {
            if (myCategory.equals(category)) {
                return true;
            }
        }
        return false;
    }

    public String makeCategoriesList() {
        int i;
        if (categories.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(categories[0]);
        for (i = 1; i < categories.length; i++) {
            sb.append(",").append(categories[i]);
        }
        return sb.toString();
    }

}


