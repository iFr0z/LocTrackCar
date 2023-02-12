package tk.ifroz.loctrackcar.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tk.ifroz.loctrackcar.R
import tk.ifroz.loctrackcar.databinding.SearchPlaceFragmentBinding
import tk.ifroz.loctrackcar.ui.viewmodel.SearchPlaceViewModel
import tk.ifroz.loctrackcar.util.extension.onEditorAction

class SearchPlaceFragment : BottomSheetDialogFragment() {

    private var _binding: SearchPlaceFragmentBinding? = null
    private val binding get() = _binding!!

    private val searchPlaceViewModel: SearchPlaceViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = SearchPlaceFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        userInterface()
    }

    private fun userInterface() {
        dialog?.window?.setSoftInputMode(SOFT_INPUT_STATE_VISIBLE)

        val requestStart = getString(R.string.search_place_request_start)
        val requestEnd = getString(R.string.search_place_request_end)
        binding.searchPlaceTiet.onEditorAction(
            IME_ACTION_SEARCH,
            coordinatesPattern,
            binding.searchPlaceTil,
            requestStart,
            requestEnd
        ) {
            searchPlaceViewModel.insertSearchPlaceResult(it)
        }

        binding.searchPlaceTil.setEndIconOnClickListener {
            binding.searchPlaceTiet.text?.clear()

            searchPlaceViewModel.deleteSearchPlaceResult()
        }

        searchPlaceViewModel.searchPlaceResults.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                val searchPlaceLatitude = it[0]
                val searchPlaceLongitude = it[1]
                val searchPlaceFormatResult = getString(
                    R.string.search_place_format, searchPlaceLatitude, searchPlaceLongitude
                )

                binding.searchPlaceTiet.setText(searchPlaceFormatResult)
                binding.searchPlaceTiet.setSelection(binding.searchPlaceTiet.text?.length!!)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val coordinatesPattern = "(^[-+]?(?:[1-8]?\\d(?:\\.\\d+)?|90(?:\\.0+)?))," +
                "\\s*([-+]?(?:180(?:\\.0+)?|(?:(?:1[0-7]\\d)|(?:[1-9]?\\d))(?:\\.\\d+)?))\$"
    }
}