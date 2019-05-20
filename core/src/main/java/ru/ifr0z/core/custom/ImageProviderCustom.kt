package ru.ifr0z.core.custom

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import com.yandex.runtime.image.ImageProvider
import ru.ifr0z.core.extension.vectorDrawableToBitmap

class ImageProviderCustom(
    private val context: Context, @DrawableRes val id: Int
) : ImageProvider() {

    override fun getImage(): Bitmap? = context.vectorDrawableToBitmap(id)

    override fun getId(): String = id.toString()
}