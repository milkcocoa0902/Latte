package com.milkcocoa.info.latte.model.proxytoken

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * プロキシトークン取得リクエストを表すクラス。
 * 
 * このクラスはプロキシサーバーのトークンエンドポイントに送信されるリクエストの
 * 形式を定義します。現在は追加のパラメータを必要としないため、空のクラスとなっています。
 */
@Serializable
@SerialName("ProxyTokenRequest")
class ProxyTokenRequest
