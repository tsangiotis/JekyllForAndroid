package com.jchanghong.model

import com.jchanghong.GlobalApplication
import java.io.Serializable

/**
 * \* Created with IntelliJ IDEA.
 * \* User: jchanghong
 * \* Date: 14/06/2016
 * \* Time: 10:00
 * \ */
data class CategoryIcon(var icon: String=GlobalApplication.db.cat_icon[0],
                        var color: String=GlobalApplication.db.cat_color[0],
                        var isChecked: Boolean = false) : Serializable
