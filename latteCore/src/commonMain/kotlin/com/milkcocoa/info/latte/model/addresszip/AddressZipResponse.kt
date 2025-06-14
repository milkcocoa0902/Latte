package com.milkcocoa.info.latte.model.addresszip

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 住所情報から郵便番号を検索した結果を表すデータクラス。
 * 
 * このクラスは日本郵便APIの住所検索エンドポイントからの応答の
 * 形式を定義します。
 *
 * @property level マッチングレベル（都道府県、市区町村、町域など）
 * @property page 現在のページ番号
 * @property limit 1ページあたりの結果数
 * @property count 検索結果の総数
 * @property addresses 検索結果の住所リスト
 */
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
