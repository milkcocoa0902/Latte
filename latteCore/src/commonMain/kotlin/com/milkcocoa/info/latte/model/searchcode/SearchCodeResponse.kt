package com.milkcocoa.info.latte.model.searchcode

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("SearchCodeResponse")
data class SearchCodeResponse(
    @SerialName("page")
    val page: Int,
    @SerialName("limit")
    val limit: Int,
    @SerialName("count")
    val count: Int,
    @SerialName("searchtype")
    val searchType: String,
    @SerialName("addresses")
    val addresses: List<SearchAddress>
)

