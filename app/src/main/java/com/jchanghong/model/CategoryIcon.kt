package com.jchanghong.model

import java.io.Serializable

/**
 * \* Created with IntelliJ IDEA.
 * \* User: jchanghong
 * \* Date: 14/06/2016
 * \* Time: 10:00
 * \ */
class CategoryIcon : Serializable {
    var icon: String? = null
    var color: String? = null
    var isChecked: Boolean = false

    constructor()

    constructor(icon: String, color: String) {
        this.icon = icon
        this.color = color
    }
}
