package app.wt.noolis.model;

import java.io.Serializable;

public class Note implements Serializable {
    private long id;
    private String tittle;
    private String content;
    private long last_edit;
    private int favourite = 0;
    private Category category;

    public Note() {
    }

    public Note(String tittle, String content, long last_edit, int favourite, Category category) {
        this.tittle = tittle;
        this.content = content;
        this.last_edit = last_edit;
        this.favourite = favourite;
        this.category = category;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTittle() {
        return tittle;
    }

    public void setTittle(String tittle) {
        this.tittle = tittle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getLastEdit() {
        return last_edit;
    }

    public void setLastEdit(long last_edit) {
        this.last_edit = last_edit;
    }

    public int getFavourite() {
        return favourite;
    }

    public void setFavourite(int favourite) {
        this.favourite = favourite;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void clear() {
        this.id = 0;
        this.tittle = null;
        this.content = null;
        this.last_edit = 0;
        this.favourite = 0;
    }
}
