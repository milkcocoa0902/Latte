package com.milkcocoa.info.latte.model.searchcode

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("SearchCodeRequest")
data class SearchCodeRequest(
    @SerialName("page")
    var page: Int = 1,
    @SerialName("limit")
    var limit: Int = 1,
    @SerialName("choikitype")
    var choiki: ChoikiType = ChoikiType.WithoutBrackets,
    @SerialName("searchtype")
    var searchType: SearchType = SearchType.WithBiz
)
