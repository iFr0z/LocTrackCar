package tk.ifroz.loctrackcar.util.extension

import android.view.Gravity
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar.make

inline fun View.snackBarBottom(string: String, length: Int = LENGTH_SHORT, f: Snackbar.() -> Unit) {
    val snackBarBottom = make(this, string, length)
    snackBarBottom.f()
    snackBarBottom.show()
}

inline fun View.snackBarTop(string: String, length: Int = LENGTH_SHORT, f: Snackbar.() -> Unit) {
    val snackBarTop = make(this, string, length)
    val snackBarView = snackBarTop.view
    val params: LayoutParams = snackBarView.layoutParams as LayoutParams
    params.gravity = Gravity.TOP
    @Suppress("DEPRECATION") snackBarView.setOnApplyWindowInsetsListener { _, insets ->
        val statusBarSize = insets.systemWindowInsetTop
        snackBarView.translationY = statusBarSize.toFloat()
        insets
    }
    snackBarView.layoutParams = params
    snackBarTop.f()
    snackBarTop.show()
}

fun Snackbar.action(string: String, listener: (View) -> Unit) {
    setAction(string, listener)
}