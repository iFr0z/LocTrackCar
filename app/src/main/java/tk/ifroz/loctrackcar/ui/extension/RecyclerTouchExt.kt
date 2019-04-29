package tk.ifroz.loctrackcar.ui.extension

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import tk.ifroz.loctrackcar.ui.adapter.RecyclerTouchListener
import tk.ifroz.loctrackcar.ui.interfaces.ClickListener

fun Context.recyclerTouch(viewRecycler: RecyclerView, onEvent: (Int?) -> Unit) {
    viewRecycler.addOnItemTouchListener(
        RecyclerTouchListener(
            this,
            viewRecycler,
            object : ClickListener {
                override fun onClick(view: View, position: Int) = onEvent.invoke(position)

                override fun onLongClick(view: View?, position: Int) {}
            }
        )
    )
}