package tk.ifroz.loctrackcar.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.search_place_fragment.view.*
import ru.ifr0z.core.extension.onEditorAction
import ru.ifr0z.core.extension.onTextChanges
import tk.ifroz.loctrackcar.R
import tk.ifroz.loctrackcar.viewmodel.SearchPlaceViewModel

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
            search_place_et.onEditorAction(
                IME_ACTION_SEARCH, coordinatesPattern, view.context, requestStart, requestEnd
            ) { arrayLatLng ->
                searchPlaceViewModel.update(arrayLatLng)

                dialog?.onBackPressed()
            }
            search_place_et.onTextChanges(clear_search_iv)

            clear_search_iv.setOnClickListener {
                search_place_et.text?.clear()

                searchPlaceViewModel.clear()
            }

            searchPlaceViewModel.searchPlaceResult.observe(
                viewLifecycleOwner, Observer { searchPlaceResult ->
                    if (!searchPlaceResult.isNullOrEmpty()) {
                        val searchPlaceLatitude = searchPlaceResult[0]
                        val searchPlaceLongitude = searchPlaceResult[1]
                        val searchPlaceFormatResult = getString(
                            R.string.search_place_format, searchPlaceLatitude, searchPlaceLongitude
                        )

                        search_place_et.setText(searchPlaceFormatResult)
                        search_place_et.setSelection(search_place_et.text?.length!!)
                    }
                }
            )
        }
    }

    companion object {
        const val coordinatesPattern = "(^[-+]?(?:[1-8]?\\d(?:\\.\\d+)?|90(?:\\.0+)?))," +
                "\\s*([-+]?(?:180(?:\\.0+)?|(?:(?:1[0-7]\\d)|(?:[1-9]?\\d))(?:\\.\\d+)?))\$"
    }
}