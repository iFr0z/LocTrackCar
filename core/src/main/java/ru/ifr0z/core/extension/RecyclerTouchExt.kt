package ru.ifr0z.core.extension

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.ifr0z.core.custom.OnItemTouchListenerCustom
import ru.ifr0z.core.interfaces.ClickListener

fun Context.recyclerTouch(viewRecycler: RecyclerView, onEvent: (Int?) -> Unit) {
    viewRecycler.addOnItemTouchListener(
        OnItemTouchListenerCustom(
            this,
            viewRecycler,
            object : ClickListener {
                override fun onClick(view: View, position: Int) = onEvent.invoke(position)

                override fun onLongClick(view: View?, position: Int) {}
            }
        )
    )
}