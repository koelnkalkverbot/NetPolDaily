package de.jenswangenheim.nptag.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class NPTag(
    var title: String? = "",
    var description: String? = "",
    var imageUrl: String? = "",
    var pub_date: Long? = 0,
    var link: String? = ""
)