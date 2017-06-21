package com.jchanghong.model

import com.jchanghong.data.DatabaseManager
import java.io.Serializable
import java.util.*

data class Note(var id: Long = 0,
                var tittle: String="",
                var content: String="",
                var lastEdit: Long = Date().time,
                var favourite: Int = 0,
                var category: Category=DatabaseManager.defaultCAT) : Serializable {
    fun clear() {
        this.id = 0
        this.tittle = ""
        this.content = ""
        this.lastEdit = 0
        this.favourite = 0
    }
    override fun equals(other: Any?): Boolean {
        if (other !is Note) {
            return false
        }
        else {
            return other.tittle==tittle&& other.content==content
        }
    }


    override fun hashCode(): Int {
        return tittle.hashCode()+content.hashCode()
    }
}
