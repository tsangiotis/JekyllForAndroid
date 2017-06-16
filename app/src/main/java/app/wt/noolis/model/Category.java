package app.wt.noolis.model;

import java.io.Serializable;

public class Category implements Serializable {
    private long id;
    private String name;
    private String color;
    private String icon;
    private int note_count = 0;

    public Category() {
    }

    public Category(long id, String name, String color, String icon) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.icon = icon;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getNote_count() {
        return note_count;
    }

    public void setNote_count(int note_count) {
        this.note_count = note_count;
    }
}
