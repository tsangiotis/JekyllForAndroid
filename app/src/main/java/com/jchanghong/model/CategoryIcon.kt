package com.jchanghong.model

import com.jchanghong.data.DatabaseManager
import java.io.Serializable

/**
 * \* Created with IntelliJ IDEA.
 * \* User: jchanghong
 * \* Date: 14/06/2016
 * \* Time: 10:00
 * \ */
data class CategoryIcon(var icon: String= DatabaseManager.cat_icon_data[0],
                        var color: String=DatabaseManager.cat_color_data[0],
                        var isChecked: Boolean = false) : Serializable
