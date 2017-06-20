package com.jchanghong.utils

import java.util.*


/**
\* Created with IntelliJ IDEA.
\* User: jchanghong
\* Date: 2017/6/20 0020
\* Time: 16:41
\*/
fun String.hasYamHead():Boolean  {
    var yamcoun: Int = 0
    try {
        var scan = Scanner(this)
        while (true) {
            var line = scan.nextLine()
            if (line == "---") {
                yamcoun++
            }
        }
    } catch(e: Exception) {
    }
    return yamcoun>=2
}

fun String.removeyam():String{
    if (!this.hasYamHead()) {
        return this
    }
    var index1 = this.indexOf("---")
    index1 = this.indexOf("---", index1 + 1)
    return this.substring(index1+5)
}