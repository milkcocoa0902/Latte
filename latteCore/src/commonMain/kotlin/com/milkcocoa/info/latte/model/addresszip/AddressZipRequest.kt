package com.milkcocoa.info.latte.model.addresszip

import com.milkcocoa.info.latte.common.BoolIntSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 住所情報から郵便番号を検索するためのリクエストを表すデータクラス。
 * 
 * このクラスは日本郵便APIの住所検索エンドポイントに送信されるリクエストの
 * パラメータを定義します。様々な住所情報の組み合わせで検索が可能です。
 *
 * @property prefCode 都道府県コード
 * @property prefName 都道府県名
 * @property prefKana 都道府県名（カナ）
 * @property prefRoma 都道府県名（ローマ字）
 * @property cityCode 市区町村コード
 * @property cityName 市区町村名
 * @property cityKana 市区町村名（カナ）
 * @property cityRoma 市区町村名（ローマ字）
 * @property townName 町域名
 * @property townKana 町域名（カナ）
 * @property townRoma 町域名（ローマ字）
 * @property freeWord フリーワード検索
 * @property flgGetCity 市区町村一覧を取得するフラグ
 * @property flgGetPref 都道府県一覧を取得するフラグ
 * @property page 取得するページ番号（デフォルト: 1）
 * @property limit 1ページあたりの結果数（デフォルト: 1000）
 */
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
