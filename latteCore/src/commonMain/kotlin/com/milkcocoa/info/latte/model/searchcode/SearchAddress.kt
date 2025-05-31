package com.milkcocoa.info.latte.model.searchcode

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("SearchAddress")
data class SearchAddress(
    @SerialName("dgacode")
    val dgaCode: String?,
    @SerialName("zip_code")
    val zipCode: String?,
    @SerialName("pref_code")
    val prefCode: String?,
    @SerialName("pref_name")
    val prefName: String?,
    @SerialName("pref_kana")
    val prefKana: String?,
    @SerialName("pref_roma")
    val prefRoma: String?,
    @SerialName("city_code")
    val cityCode: Int?,
    @SerialName("city_name")
    val cityName: String?,
    @SerialName("city_kana")
    val cityKana: String?,
    @SerialName("city_roma")
    val cityRoma: String?,
    @SerialName("town_name")
    val townName: String?,
    @SerialName("town_kana")
    val townKana: String?,
    @SerialName("town_roma")
    val townRoma: String?,
    @SerialName("biz_name")
    val bizName: String?,
    @SerialName("biz_kana")
    val bizKana: String?,
    @SerialName("biz_roma")
    val bizRoma: String?,
    @SerialName("block_name")
    val blockName: String?,
    @SerialName("other_name")
    val otherName: String?,
    @SerialName("address")
    val address: String?,
    @SerialName("longitude")
    val longitude: Double?,
    @SerialName("latitude")
    val latitude: Double?,
)