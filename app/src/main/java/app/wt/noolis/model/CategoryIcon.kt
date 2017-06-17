package app.wt.noolis.model

import java.io.Serializable

/**
 * Created by Kodok on 14/06/2016.
 */
class CategoryIcon : Serializable {
    var icon: String? = null
    var color: String? = null
    var isChecked: Boolean = false

    constructor() {}

    constructor(icon: String, color: String) {
        this.icon = icon
        this.color = color
    }
}
