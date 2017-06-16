package app.wt.noolis.model;

import java.io.Serializable;

/**
 * Created by Kodok on 14/06/2016.
 */
public class CategoryIcon implements Serializable {
    private String icon;
    private String color;
    private boolean checked;

    public CategoryIcon() {
    }

    public CategoryIcon(String icon, String color) {
        this.icon = icon;
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
