package tk.ifroz.loctrackcar.api

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import retrofit2.Retrofit.Builder
import retrofit2.converter.moshi.MoshiConverterFactory.create

object GeocoderApiClient {

    private const val baseUrl = "https://geocode-maps.yandex.ru/"

    fun getClient(): GeocoderApiInterface {
        return Builder().baseUrl(baseUrl)
            .addConverterFactory(create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory()).build()
            .create(GeocoderApiInterface::class.java)
    }
}