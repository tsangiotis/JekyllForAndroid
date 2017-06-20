package com.jchanghong.model


/**
\* Created with IntelliJ IDEA.
\* User: jchanghong
\* Date: 2017/6/20 0020
\* Time: 12:44
\*/
object NoteCache : ArrayList<Note>() {
    fun remove(lo: Long): {
        var i=find { it.id==lo }
        remove(i)
    }
}