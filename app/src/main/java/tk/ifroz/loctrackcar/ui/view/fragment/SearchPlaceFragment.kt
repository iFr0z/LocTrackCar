package tk.ifroz.loctrackcar.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.search_place_fragment.view.*
import tk.ifroz.loctrackcar.R
import tk.ifroz.loctrackcar.ui.viewmodel.SearchPlaceViewModel
import tk.ifroz.loctrackcar.util.extension.onEditorAction

class SearchPlaceFragment : BottomSheetDialogFragment() {

    private val searchPlaceViewModel: SearchPlaceViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.search_place_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        userInterface(view)
    }

    private fun userInterface(view: View) {
        view.apply {
            dialog?.window?.setSoftInputMode(SOFT_INPUT_STATE_VISIBLE)

            val requestStart = getString(R.string.search_place_request_start)
            val requestEnd = getString(R.string.search_place_request_end)
            search_place_tiet.onEditorAction(
                IME_ACTION_SEARCH, coordinatesPattern, search_place_til, requestStart, requestEnd
            ) {
                searchPlaceViewModel.insertSearchPlaceResult(it)

                dialog?.onBackPressed()
            }

            search_place_til.setEndIconOnClickListener {
                search_place_tiet.text?.clear()

                searchPlaceViewModel.deleteSearchPlaceResult()
            }

            searchPlaceViewModel.searchPlaceResults.observe(viewLifecycleOwner, {
                if (!it.isNullOrEmpty()) {
                    val searchPlaceLatitude = it[0]
                    val searchPlaceLongitude = it[1]
                    val searchPlaceFormatResult = getString(
                        R.string.search_place_format, searchPlaceLatitude, searchPlaceLongitude
                    )

                    search_place_tiet.setText(searchPlaceFormatResult)
                    search_place_tiet.setSelection(search_place_tiet.text?.length!!)
                }
            })
        }
    }

    companion object {
        const val coordinatesPattern = "(^[-+]?(?:[1-8]?\\d(?:\\.\\d+)?|90(?:\\.0+)?))," +
                "\\s*([-+]?(?:180(?:\\.0+)?|(?:(?:1[0-7]\\d)|(?:[1-9]?\\d))(?:\\.\\d+)?))\$"
    }
}