# Latte
郵便番号・デジタルアドレスAPI クライアントライブラリ for Kotlin


✅ 各種APIに対応  
✅ 直接利用、あるいはプロキシ利用を想定したインターフェース（プロキシ利用は構想段階）  
✅ トークンの自動再利用  
✅ Kotlin Coroutines対応


## 導入方法
TBD

## 利用方法（クライアント）

### クライアントの作成
APIを利用するにはクライアントを作成します。  
下記の例は、APIに直接接続する（=プロキシを利用しない）例です。  

```kotlin
val latte = Latte.of(
  url = "https://stb-ssss.da.pf.japanpost.jp",
  clientId = "<your client id>",
  secretKey = "your secret key",
  forwardedFor = ""
)
```

### トークンの取得
最新のトークンを取得するには、 `token()` を実行します。  
副作用として、 `Latte`インスタンス内部のトークンストアの情報が更新されます。

```kotlin
val token = latte.token()
```

また、 `withToken()` を用いることで、期限内のトークンを再利用しながら後続の処理が実行可能です。  
なお「期限内」とは、前回の取得から420秒以内を指します。（トークンの期限が600秒）

```kotlin
latte.withToken { token ->
    
}
```

## 住所の検索
デジタルアドレス/郵便番号/事業所個別番号などを用いて住所を検索します。

```kotlin
latte.withToken { token ->
    latte.search(
        token = token,
        searchCode = "100",
        params = SearchCodeRequest(
            limit = 50,
            choiki = ChoikiType.WithoutBrackets
        )
    )
}
```

## 郵便番号の検索
住所に関する複数の条件を用いて、郵便番号を検索します。

```kotlin
latte.withToken { token ->
    latte.addressZip(
        token = token
    ){
        this.prefCode = "13"
        this.cityName = "足立区"
    }.let {
        println(it)
    }
}
```


## 利用方法（プロキシの構築）
TBD