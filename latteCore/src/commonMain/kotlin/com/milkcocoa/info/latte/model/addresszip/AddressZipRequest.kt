package com.milkcocoa.info.latte.model.addresszip

import com.milkcocoa.info.latte.common.BoolIntSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("AddressZipRequest")
data class AddressZipRequest(
    @SerialName("pref_code")
    var prefCode: String = "",
    @SerialName("pref_name")
    var prefName: String = "",
    @SerialName("pref_kana")
    var prefKana: String = "",
    @SerialName("pref_roma")
    var prefRoma: String = "",
    @SerialName("city_code")
    var cityCode: String = "",
    @SerialName("city_name")
    var cityName: String = "",
    @SerialName("city_kana")
    var cityKana: String = "",
    @SerialName("city_roma")
    var cityRoma: String = "",
    @SerialName("town_name")
    var townName: String = "",
    @SerialName("town_kana")
    var townKana: String = "",
    @SerialName("town_roma")
    var townRoma: String = "",
    @SerialName("freeword")
    var freeWord: String = "",
    @Serializable(with = BoolIntSerializer::class)
    @SerialName("flg_getcity")
    var flgGetCity: Boolean = false,
    @Serializable(with = BoolIntSerializer::class)
    @SerialName("flg_getpref")
    var flgGetPref: Boolean = false,
    @SerialName("page")
    var page: Int = 1,
    @SerialName("limit")
    var limit: Int = 1000
)
