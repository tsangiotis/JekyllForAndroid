package com.jchanghong.utils

import java.util.*


/**
\* Created with IntelliJ IDEA.
\* User: jchanghong
\* Date: 2017/6/20 0020
\* Time: 16:41
\*/
fun String.hasYamHead(): Boolean {
    var yamcoun: Int = 0
    try {
        val scan = Scanner(this)
        while (true) {
            val line = scan.nextLine()
            if (line == "---") {
                yamcoun++
            }
        }
    } catch(e: Exception) {
    }
    return yamcoun >= 2
}

fun String.removeyam(): String {
    if (!this.hasYamHead()) {
        return this
    }
    var index1 = this.indexOf("---")
    index1 = this.indexOf("---", index1 + 1)
    return this.substring(index1 + 3)
}

fun String.getyam(): String {
    if (!this.hasYamHead()) {
        return ""
    }
    val index1 = this.indexOf("---")
    val index2 = this.indexOf("---", index1 + 1)
    return this.substring(index1 + 4, index2)
}

fun String.date_id_toTitle(): String {
    val index1 = this.lastIndexOf("-")
    var index2 = this.lastIndexOf(".")
    if (index2 < 1) index2 = this.length
    if (index1 < index2) {
        return this.substring(index1 + 1, index2)
    } else {
        return this
    }
}

fun String.path2Catogery(): String? {
    try {
        val i2 = this.lastIndexOf("/")
        val i1 = this.lastIndexOf("/", i2 - 1)
        if (i2 > i1) {
            return this.substring(i1 + 1, i2)
        } else {
            return null
        }
    } catch(e: Exception) {
        return null
    }
}