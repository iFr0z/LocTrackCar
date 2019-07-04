package tk.ifroz.loctrackcar.api

import retrofit2.Retrofit.Builder
import retrofit2.converter.moshi.MoshiConverterFactory.create

object GeocoderApiClient {

    private const val baseUrl = "https://geocode-maps.yandex.ru/1.x/"

    fun getClient(): GeocoderApi {
        return Builder().baseUrl(baseUrl).addConverterFactory(create()).build()
            .create(GeocoderApi::class.java)
    }
}