package tk.ifroz.loctrackcar.util.custom

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import com.yandex.runtime.image.ImageProvider
import tk.ifroz.loctrackcar.util.extension.vectorDrawableToBitmap

class ImageProviderCustom(
    private val context: Context, @DrawableRes val id: Int
) : ImageProvider() {

    override fun getImage(): Bitmap? = context.vectorDrawableToBitmap(id)

    override fun getId(): String = id.toString()
}