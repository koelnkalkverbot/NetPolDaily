package de.jenswangenheim.nptag

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ShareCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso
import de.jenswangenheim.nptag.model.NPTag
import de.jenswangenheim.nptag.model.NPTagViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        const val NOTIFICATION_TOPIC = "news"
        const val DATABASE_CHILD = "np_tag"
        const val ITEM_LIMIT = 15
        const val ALT_URL = "https://cdn.netzpolitik.org/wp-upload/2019/03/Was_vom_Tage_uebrig_blieb_11_Maerz_2019-e1552322531481-860x484.jpg"
    }

    private lateinit var adapter: FirebaseRecyclerAdapter<NPTag, NPTagViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.setHasFixedSize(true)

        FirebaseMessaging.getInstance().subscribeToTopic(NOTIFICATION_TOPIC)

        val query = FirebaseDatabase.getInstance().reference.child(DATABASE_CHILD).limitToLast(ITEM_LIMIT)
        val options = FirebaseRecyclerOptions.Builder<NPTag>()
            .setQuery(query, NPTag::class.java)
            .build()

        adapter = object : FirebaseRecyclerAdapter<NPTag, NPTagViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, p1: Int): NPTagViewHolder {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
                return NPTagViewHolder(v)
            }

            override fun onBindViewHolder(viewHolder: NPTagViewHolder, position: Int, model: NPTag) {
                with (model) {
                    var url = imageUrl
                    if (url.isNullOrEmpty()) {
                        url = ALT_URL
                    }

                    Picasso.get().load(url).into(viewHolder.ivImage)
                    viewHolder.tvTitle.text = title
                    viewHolder.tvDescription.text = stripHtml(description)
                    viewHolder.tvDate.text = getDateTime(pub_date)
                    viewHolder.cvContainer.setOnClickListener {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                    }
                }
            }

            // reverse order, so we have the latest item as the first one in the list
            override fun getItem(position: Int): NPTag {
                return super.getItem(itemCount - 1 - position)
            }
        }
        recycler_view.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        adapter.stopListening()
        super.onStop()
    }

    private fun stripHtml(raw: String?): String? {
        return Html.fromHtml(raw).toString()
    }

    private fun getDateTime(date: Long?): String? {
        return try {
            val sdf = SimpleDateFormat("EEEE, dd. MMM", Locale.GERMAN)
            val netDate = Date(date!!)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }
}
