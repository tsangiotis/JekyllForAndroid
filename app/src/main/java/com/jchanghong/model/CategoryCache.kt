package com.jchanghong.model


/**
\* Created with IntelliJ IDEA.
\* User: jchanghong
\* Date: 2017/6/20 0020
\* Time: 12:48
\*/
object CategoryCache : ArrayList<Category>() {
    fun remove(id: Long){
        var i=find { it.id==id }
        remove(i)
    }

    fun updateme(id: Long, cat: Category) {
        var me=find { it.id == id }
        if (me != null) {
            me.name = cat.name
            me.color = cat.color
            me.note_count = cat.note_count
            me.icon = cat.icon
        }
    }
}