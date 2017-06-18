package com.jchanghong.model

import java.io.Serializable

data class Note(var id: Long = 0,
                var tittle: String="",
                var content: String="",
                var lastEdit: Long = 0,
                var favourite: Int = 0,
                var category: Category?=null) : Serializable {

    fun clear() {
        this.id = 0
        this.tittle = ""
        this.content = ""
        this.lastEdit = 0
        this.favourite = 0
    }
}
