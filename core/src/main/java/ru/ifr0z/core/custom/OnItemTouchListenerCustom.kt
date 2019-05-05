package ru.ifr0z.core.custom

import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import ru.ifr0z.core.interfaces.ClickListener

class OnItemTouchListenerCustom(
    context: Context, recyclerView: RecyclerView, private val clickListener: ClickListener?
) : OnItemTouchListener {

    private val gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
            override fun onSingleTapUp(motionEvent: MotionEvent): Boolean = true

            override fun onLongPress(motionEvent: MotionEvent) {
                val child = recyclerView.findChildViewUnder(motionEvent.x, motionEvent.y)
                if (child != null && clickListener != null) {
                    clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child))
                }
            }
        })
    }

    override fun onInterceptTouchEvent(
        recyclerView: RecyclerView, motionEvent: MotionEvent
    ): Boolean {
        val child = recyclerView.findChildViewUnder(motionEvent.x, motionEvent.y)
        if (child != null && clickListener != null && gestureDetector.onTouchEvent(motionEvent)) {
            clickListener.onClick(child, recyclerView.getChildAdapterPosition(child))
        }
        return false
    }

    override fun onTouchEvent(recyclerView: RecyclerView, motionEvent: MotionEvent) {}

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
}