package tk.ifroz.loctrackcar.data.api

import retrofit2.Retrofit.Builder
import retrofit2.converter.moshi.MoshiConverterFactory.create

object AddressApiBuilder {

    private const val baseUrl = "https://geocode-maps.yandex.ru/1.x/"

    private fun getAddressApi() = Builder().baseUrl(baseUrl).addConverterFactory(create()).build()

    val addressApiService: AddressApiService = getAddressApi().create(AddressApiService::class.java)
}