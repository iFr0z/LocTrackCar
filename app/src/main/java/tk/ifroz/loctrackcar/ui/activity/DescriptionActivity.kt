package tk.ifroz.loctrackcar.ui.activity

import android.os.Bundle
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders.of
import com.klinker.android.sliding.SlidingActivity
import kotlinx.android.synthetic.main.activity_description.*
import tk.ifroz.loctrackcar.R
import tk.ifroz.loctrackcar.db.entity.Description
import tk.ifroz.loctrackcar.viewmodel.MarkerCarViewModel

class DescriptionActivity : SlidingActivity() {

    private lateinit var markerCarViewModel: MarkerCarViewModel

    override fun init(savedInstanceState: Bundle?) {
        setContent(R.layout.activity_description)

        userInterface()
    }

    private fun userInterface() {
        val titleDescription = getString(R.string.description_title)
        title = titleDescription

        val colorPrimary = getColor(this, R.color.colorPrimary)
        val colorPrimaryDark = getColor(this, R.color.colorPrimaryDark)
        setPrimaryColors(colorPrimary, colorPrimaryDark)

        enableFullscreen()

        markerCarViewModel = of(this).get(MarkerCarViewModel::class.java)
        markerCarViewModel.descriptions.observe(this, Observer { description ->
            description?.let {
                description_et.setText(description.description)
            }
        })

        setFab(colorPrimaryDark, R.drawable.ic_save_white_24dp) {
            if (description_et.text.isNotEmpty()) {
                val textDescription = description_et.text.toString()
                description_et.setText(textDescription)

                markerCarViewModel.insertDescription(Description(textDescription))

                finish()
            }
        }
    }
}