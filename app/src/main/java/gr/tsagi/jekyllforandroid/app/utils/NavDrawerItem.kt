package gr.tsagi.jekyllforandroid.app.utils

/**
 * Created by tsagi on 7/9/14.
 */
class NavDrawerItem {

    var title: String? = null
    var icon: Int = 0


    constructor() {}

    constructor(title: String, icon: Int) {
        this.title = title
        this.icon = icon
    }

}