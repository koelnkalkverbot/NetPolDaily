package de.jenswangenheim.nptag

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.*
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso
import de.jenswangenheim.nptag.model.FeedItem
import de.jenswangenheim.nptag.model.FeedItemViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        const val NOTIFICATION_TOPIC = "news"
        const val DATABASE_CHILD = "np_tag"
        const val ITEM_LIMIT = 15
        const val DATE_FORMAT_PATTERN = "EEEE, dd. MMMM"
        const val DONATION_URL = "https://netzpolitik.org/spenden/"
        const val ALT_IMAGE_URL = "https://cdn.netzpolitik.org/wp-upload/2019/03/Was_vom_Tage_uebrig_blieb_11_Maerz_2019-e1552322531481-860x484.jpg"
    }

    private lateinit var adapter: FirebaseRecyclerAdapter<FeedItem, FeedItemViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.setHasFixedSize(true)

        if (getNotificationsEnabled()) {
            FirebaseMessaging.getInstance().subscribeToTopic(NOTIFICATION_TOPIC)
        }

        val query = FirebaseDatabase.getInstance().reference.child(DATABASE_CHILD).limitToLast(ITEM_LIMIT)
        val options = FirebaseRecyclerOptions.Builder<FeedItem>()
            .setQuery(query, FeedItem::class.java)
            .build()

        adapter = object : FirebaseRecyclerAdapter<FeedItem, FeedItemViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, p1: Int): FeedItemViewHolder {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
                return FeedItemViewHolder(v)
            }

            override fun onBindViewHolder(viewHolder: FeedItemViewHolder, position: Int, model: FeedItem) {
                with (model) {
                    var url = image_url
                    if (url.isNullOrEmpty()) {
                        url = ALT_IMAGE_URL
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
            override fun getItem(position: Int): FeedItem {
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        if (!getNotificationsEnabled()) {
            menu.findItem(R.id.notifications).setIcon(R.drawable.ic_notification_off)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.donate -> {
                openUrl()
                true
            }
            R.id.notifications -> {
                toggleNotifications(item)
                true
            }
            R.id.about -> {
                showAboutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleNotifications(item: MenuItem) {
        val enabled = getNotificationsEnabled()
        if (enabled) {
            setNotificationsEnabled(false)
            item.setIcon(R.drawable.ic_notification_off)
            FirebaseMessaging.getInstance().unsubscribeFromTopic(NOTIFICATION_TOPIC)
        } else {
            setNotificationsEnabled(true)
            item.setIcon(R.drawable.ic_notification_on)
            FirebaseMessaging.getInstance().subscribeToTopic(NOTIFICATION_TOPIC)
        }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.about)
            .setMessage(Html.fromHtml(getString(R.string.about_message)))
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun openUrl() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(DONATION_URL)))
    }

    private fun stripHtml(raw: String?): String? {
        return Html.fromHtml(raw).toString()
    }

    private fun getNotificationsEnabled(): Boolean {
        val sharedPref = getSharedPreferences(this.localClassName, Context.MODE_PRIVATE)
        return sharedPref.getBoolean(getString(R.string.pref_notifications), true)
    }

    private fun setNotificationsEnabled(enabled: Boolean) {
        val sharedPref = getSharedPreferences(this.localClassName, Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean(getString(R.string.pref_notifications), enabled).apply()
    }

    private fun getDateTime(date: Long?): String? {
        return try {
            val sdf = SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.GERMAN)
            val netDate = Date(date!!)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }
}
