package com.milkcocoa.info.latte.model.addresszip

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Address")
data class Address(
    @SerialName("zip_code")
    val zipCode: String,
    @SerialName("pref_code")
    val prefCode: String,
    @SerialName("pref_name")
    val prefName: String,
    @SerialName("pref_kana")
    val prefKana: String,
    @SerialName("pref_roma")
    val prefRoma: String,
    @SerialName("city_code")
    val cityCode: String,
    @SerialName("city_name")
    val cityName: String,
    @SerialName("city_kana")
    val cityKana: String,
    @SerialName("city_roma")
    val cityRoma: String,
    @SerialName("town_name")
    val townName: String,
    @SerialName("town_kana")
    val townKana: String,
    @SerialName("town_roma")
    val townRoma: String
)