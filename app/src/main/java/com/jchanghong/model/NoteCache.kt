package com.jchanghong.model


/**
\* Created with IntelliJ IDEA.
\* User: jchanghong
\* Date: 2017/6/20 0020
\* Time: 12:44
\*/
object NoteCache : ArrayList<Note>() {
    fun remove(lo: Long){
        var i=find { it.id==lo }
        remove(i)
    }
    fun getnote(title: String): Note? =find { it.tittle==title }
    fun updatenote(id: Long, note: Note) {
        var me=find { it.id == id }
        if (me != null) {
            me.tittle = note.tittle
            me.lastEdit = note.lastEdit
            me.category = note.category
            me.content = note.content
            me.favourite=note.favourite
        }
    }
}