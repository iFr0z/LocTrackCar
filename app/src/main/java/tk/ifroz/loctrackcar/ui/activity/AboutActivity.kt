package tk.ifroz.loctrackcar.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import kotlinx.android.synthetic.main.activity_about.*
import org.jetbrains.anko.browse
import org.jetbrains.anko.email
import ru.ifr0z.core.extension.recyclerTouch
import tk.ifroz.loctrackcar.R
import tk.ifroz.loctrackcar.ui.adapter.Item
import tk.ifroz.loctrackcar.ui.adapter.ItemAdapter
import java.util.*

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        userInterface()
    }

    private fun userInterface() {
        setSupportActionBar(about_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        about_toolbar.setNavigationOnClickListener {
            finish()
        }

        val appVersion = getString(R.string.app_version)
        collapsing_toolbar_l.title = appVersion
        val colorWhite = getColor(this, R.color.colorNull)
        collapsing_toolbar_l.setExpandedTitleColor(colorWhite)
        collapsing_toolbar_l.setCollapsedTitleTextColor(colorWhite)

        val itemList = ArrayList<Item>()
        val titleSupport = getString(R.string.support_title)
        val subtitleSupport = getString(R.string.support_subtitle)
        itemList.add(Item(R.drawable.ic_send_white_24dp, titleSupport, subtitleSupport))
        val titlePrivacyPolicy = getString(R.string.privacy_policy_title)
        val subtitlePrivacyPolicy = getString(R.string.privacy_policy_subtitle)
        itemList.add(
            Item(R.drawable.ic_warning_white_24dp, titlePrivacyPolicy, subtitlePrivacyPolicy)
        )

        recycler_v.addItemDecoration(DividerItemDecoration(this, VERTICAL))
        recycler_v.layoutManager = LinearLayoutManager(this)
        recycler_v.itemAnimator = DefaultItemAnimator()
        recycler_v.adapter = ItemAdapter(itemList)
        recycler_v.setHasFixedSize(true)

        this.recyclerTouch(recycler_v) { position ->
            when (position) {
                0 -> {
                    val supportMail = getString(R.string.support_mail)
                    val appName = getString(R.string.app_name)
                    email(supportMail, appName, "")
                }
                1 -> {
                    val privacyPolicyUrl = getString(R.string.privacy_policy_url)
                    browse(privacyPolicyUrl)
                }
            }
        }
    }
}