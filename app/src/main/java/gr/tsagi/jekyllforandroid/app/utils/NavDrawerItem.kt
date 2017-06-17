package gr.tsagi.jekyllforandroid.app.utils

/**
\* Created with IntelliJ IDEA.
\* User: tsagi
\* Date: 7/9/14
\* Time: 9:15
\*/
class NavDrawerItem {

    var title: String? = null
    var icon: Int = 0


    constructor()

    constructor(title: String, icon: Int) {
        this.title = title
        this.icon = icon
    }

}