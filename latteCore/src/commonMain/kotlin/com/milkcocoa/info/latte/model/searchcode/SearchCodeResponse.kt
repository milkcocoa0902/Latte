package com.milkcocoa.info.latte.model.searchcode

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 郵便番号やデジタルアドレスから住所を検索した結果を表すデータクラス。
 * 
 * このクラスは日本郵便APIの検索エンドポイントからの応答の
 * 形式を定義します。
 *
 * @property page 現在のページ番号
 * @property limit 1ページあたりの結果数
 * @property count 検索結果の総数
 * @property searchType 使用された検索タイプ
 * @property addresses 検索結果の住所リスト
 */
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
