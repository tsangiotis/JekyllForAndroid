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
}