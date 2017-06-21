package com.jchanghong.model

import com.jchanghong.data.DatabaseManager
import java.io.Serializable

data class Category(var id: Long = 0,
                    var name: String = DatabaseManager.cat_name[0],
                    var color: String = DatabaseManager.cat_color[0],
                    var icon: String = DatabaseManager.cat_icon[0],
                    var note_count: Int = 0) : Serializable {
    fun create() {
        DatabaseManager.insertCategory(this)
    }

    override fun equals(other: Any?) =
            if (other !is Category) {
                false
            } else {
                other.name == name
            }

    override fun hashCode(): Int {
        return name.hashCode()
    }

}
