package com.milkcocoa.info.latte.model.addresszip

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("AddressZipResponse")
data class AddressZipResponse(
    @SerialName("level")
    val level: MatchLevel,
    @SerialName("page")
    val page: Int,
    @SerialName("limit")
    val limit: Int,
    @SerialName("count")
    val count: Int,
    @SerialName("addresses")
    val addresses: List<Address>
)
