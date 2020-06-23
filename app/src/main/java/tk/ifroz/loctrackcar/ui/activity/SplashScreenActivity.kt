package tk.ifroz.loctrackcar.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.splash_screen_activity.*
import ru.ifr0z.core.extension.transitionListener
import tk.ifroz.loctrackcar.R

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen_activity)

        userInterface()
    }

    private fun userInterface() {
        motionLayout.transitionListener(
            this@SplashScreenActivity, Intent(this@SplashScreenActivity, MainActivity::class.java)
        )
    }

    override fun onResume() {
        super.onResume()
        motionLayout.startLayoutAnimation()
    }
}