package de.jenswangenheim.nptag.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FeedItem(
    var title: String? = "",
    var description: String? = "",
    var image_url: String? = "",
    var pub_date: Long? = 0,
    var link: String? = ""
)