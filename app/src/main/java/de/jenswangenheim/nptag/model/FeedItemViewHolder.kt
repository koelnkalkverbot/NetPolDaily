package de.jenswangenheim.nptag.model

import android.support.v7.widget.CardView
import android.widget.TextView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import de.jenswangenheim.nptag.R

class FeedItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    internal var cvContainer: CardView = v.findViewById(R.id.cv_container)
    internal var ivImage: ImageView = v.findViewById(R.id.iv_teaser_image)
    internal var tvTitle: TextView = v.findViewById(R.id.tv_teaser_title)
    internal var tvDescription: TextView = v.findViewById(R.id.tv_teaser_description)
    internal var tvDate: TextView = v.findViewById(R.id.tv_teaser_date)
}