package app.wt.noolis.model

import java.io.Serializable

class Note : Serializable {
    var id: Long = 0
    var tittle: String? = null
    var content: String? = null
    var lastEdit: Long = 0
    var favourite = 0
    var category: Category? = null

    constructor()

    constructor(tittle: String, content: String, last_edit: Long, favourite: Int, category: Category) {
        this.tittle = tittle
        this.content = content
        this.lastEdit = last_edit
        this.favourite = favourite
        this.category = category
    }

    fun clear() {
        this.id = 0
        this.tittle = null
        this.content = null
        this.lastEdit = 0
        this.favourite = 0
    }
}
