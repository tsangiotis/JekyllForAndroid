package gr.tsagi.jekyllforandroid.app.util;

/**
 * Created by tsagi on 7/9/14.
 */
public class NavDrawerItem {

    private String title;
    private int icon;


    public NavDrawerItem(){}

    public NavDrawerItem(String title, int icon){
        this.title = title;
        this.icon = icon;
    }

    public String getTitle(){
        return this.title;
    }

    public int getIcon(){
        return this.icon;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setIcon(int icon){
        this.icon = icon;
    }

}