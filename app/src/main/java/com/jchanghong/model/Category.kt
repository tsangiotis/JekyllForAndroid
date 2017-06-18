package com.jchanghong.model

import com.jchanghong.GlobalApplication
import java.io.Serializable

data class Category(var id: Long = 0,
                    var name: String=GlobalApplication.db.cat_name[0],
                    var color: String=GlobalApplication.db.cat_color[0],
                    var icon: String=GlobalApplication.db.cat_icon[0],
                    var note_count: Int = 0) : Serializable
