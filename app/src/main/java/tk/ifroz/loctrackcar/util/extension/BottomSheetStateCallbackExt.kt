package tk.ifroz.loctrackcar.util.extension

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior.from

fun View.bottomSheetStateCallback(onEvent: (Int?) -> Unit) = from(this).addBottomSheetCallback(
    object : BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, state: Int) = onEvent.invoke(state)

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }
)