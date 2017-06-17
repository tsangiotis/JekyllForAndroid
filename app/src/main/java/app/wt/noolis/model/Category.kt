package app.wt.noolis.model

import java.io.Serializable

class Category : Serializable {
    var id: Long = 0
    var name: String? = null
    var color: String? = null
    var icon: String? = null
    var note_count = 0

    constructor()

    constructor(id: Long, name: String, color: String, icon: String) {
        this.id = id
        this.name = name
        this.color = color
        this.icon = icon
    }
}
