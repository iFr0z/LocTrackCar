package tk.ifroz.loctrackcar.util.extension

import android.content.Context
import android.content.Intent
import androidx.constraintlayout.motion.widget.MotionLayout

fun MotionLayout.transitionListener(context: Context, intent: Intent) {
    setTransitionListener(
        object : MotionLayout.TransitionListener {
            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                context.startActivity(intent)
            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {}

            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {}

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}
        }
    )
}