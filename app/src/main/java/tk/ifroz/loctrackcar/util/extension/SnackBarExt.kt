package tk.ifroz.loctrackcar.util.extension

import android.view.Gravity
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar.make

inline fun View.snackBarBottom(string: String, length: Int = LENGTH_LONG, f: Snackbar.() -> Unit) {
    val snackBarBottom = make(this, string, length)
    snackBarBottom.f()
    snackBarBottom.show()
}

inline fun View.snackBarTop(string: String, length: Int = LENGTH_LONG, f: Snackbar.() -> Unit) {
    val snackBarTop = make(this, string, length)
    val snackBarView = snackBarTop.view
    val params: LayoutParams = snackBarView.layoutParams as LayoutParams
    params.gravity = Gravity.TOP
//    ViewCompat.setOnApplyWindowInsetsListener(snackBarView) { _, windowInsets ->
//        val insets = windowInsets.getInsets(
//            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
//        )
//        snackBarView.translationY = insets.top.toFloat()
//        snackBarView.updatePadding(0, insets.top, 0, 0)
//        WindowInsetsCompat.CONSUMED
//    }
    snackBarView.layoutParams = params
    snackBarTop.f()
    snackBarTop.show()
}

fun Snackbar.action(string: String, listener: (View) -> Unit) {
    setAction(string, listener)
}