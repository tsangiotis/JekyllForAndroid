package com.jchanghong.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * \* Created with IntelliJ IDEA.
 * \* User: jchanghong
 * \* Date: 2017/6/20 0020
 * \* Time: 16:42
 * \
 */
class StringsKtTest {
    var test1="""
---

dddddd
dsdsd

---

dsdsd
   """
   var test2="dsfdfsdfsd\n  dsds \nfaf"
    @Test
    fun hasYamHead() {
        assertEquals(true,test1.hasYamHead())
        assertEquals(false,test2.hasYamHead())
    }

}