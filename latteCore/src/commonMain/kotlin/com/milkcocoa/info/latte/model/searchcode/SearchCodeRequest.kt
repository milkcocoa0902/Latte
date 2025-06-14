package com.milkcocoa.info.latte.model.searchcode

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 郵便番号やデジタルアドレスから住所を検索するためのリクエストを表すデータクラス。
 * 
 * このクラスは日本郵便APIの検索エンドポイントに送信されるリクエストの
 * パラメータを定義します。
 *
 * @property page 取得するページ番号（デフォルト: 1）
 * @property limit 1ページあたりの結果数（デフォルト: 1）
 * @property choiki 町域の表示形式（デフォルト: 括弧なし）
 * @property searchType 検索タイプ（デフォルト: 事業所を含む）
 */
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
