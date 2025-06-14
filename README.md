# Latte

郵便番号・デジタルアドレスAPI クライアントライブラリ for Kotlin (Postal Code and Digital Address API Client Library for Kotlin)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Latte は日本郵便のデジタルアドレス・郵便番号APIにアクセスするためのKotlinマルチプラットフォームライブラリです。JVM、Android、iOS、WebAssembly JSをサポートしています。

## English Summary

Latte is a Kotlin Multiplatform library for accessing Japan Post's Digital Address and Postal Code APIs. It supports JVM, Android, iOS, and WebAssembly JS platforms.

### Key Features
- Support for various Japan Post APIs
- Direct API access or proxy-based interface
- Automatic token reuse
- Kotlin Coroutines support
- Cross-platform (JVM, Android, iOS, WebAssembly JS)

### Quick Start
```kotlin
// Create client
val latte = Latte.of(
  url = "https://stb-ssss.da.pf.japanpost.jp",
  clientId = "<your client id>",
  secretKey = "<your secret key>",
  forwardedFor = ""
)

// Use with token management
latte.withToken { token ->
    // Search address by postal code
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

### Proxy Server Setup

You can also set up a proxy server to:
- Hide API credentials from client applications
- Share credentials across multiple clients
- Manage and limit API call rates
- Simplify client implementation

1. Configure environment variables in `.env` file:
```
# Japan Post API settings
LATTE_ENDPOINT_URL=https://stb-ssss.da.pf.japanpost.jp
LATTE_CLIENT_ID=your_client_id
LATTE_SECRET_KEY=your_secret_key

# Proxy server settings
LATTE_SERVER_PORT=12345
LATTE_TOKEN_REFILL_PERIOD=300
LATTE_CALL_TOKEN_COUNT=60
```

2. Start the proxy server:
```bash
cd server
./gradlew run
```

3. Use the proxy from client applications:
```kotlin
// Create client with proxy URL
val latte = Latte.of("http://localhost:12345")

// Use API as normal
latte.withToken { token ->
    // API calls...
}
```

#### Proxy Server Security

The proxy server is protected by API keys to ensure secure access:

- All API endpoints (except `/proxytoken`) require an `X-API-KEY` header for authentication
- Clients can obtain an API key by calling the `/proxytoken` endpoint
- The DefaultCredentialsProvider automatically handles API key acquisition and renewal and is specifically designed to work with the latte proxy server implementation

For custom authentication, you can implement your own CredentialsProvider:

```kotlin
// Create a custom credentials provider
class MyCredentialsProvider : CredentialsProvider {
    override suspend fun provide(host: String): Pair<String, String> {
        // Return header name and value
        return "X-API-KEY" to "your-api-key"
    }
}

// Use it with the proxy connection
val latte = Latte.of(
    ConnectionInfo.Proxy("http://localhost:12345")
        .with(MyCredentialsProvider())
)
```

For more details, see the Japanese documentation below.

---

## 特徴

✅ 各種APIに対応  
✅ 直接利用、あるいはプロキシ利用を想定したインターフェース（プロキシ利用は構想段階）  
✅ トークンの自動再利用  
✅ Kotlin Coroutines対応  
✅ クロスプラットフォーム（JVM、Android、iOS、WebAssembly JS）

## アーキテクチャ

Latteは以下のコンポーネントで構成されています：

- **latteCore**: クライアントライブラリのコア機能を提供
- **server**: プロキシサーバーの実装（開発中）
- **composeApp**: サンプルアプリケーション

## 導入方法

### Gradle
TBD

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        // リポジトリの設定
        mavenCentral()
        // 必要に応じてGitHubパッケージなどを追加
    }
}

// build.gradle.kts
dependencies {
    implementation("com.milkcocoa.info:latte-core:0.1.0") 
}
```

## 利用方法（クライアント）

### クライアントの作成
APIを利用するにはクライアントを作成します。  
下記の例は、APIに直接接続する（=プロキシを利用しない）例です。  

```kotlin
val latte = Latte.of(
  url = "https://stb-ssss.da.pf.japanpost.jp",
  clientId = "<your client id>",
  secretKey = "<your secret key>",
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

プロキシサーバーを構築することで、以下のメリットがあります：

1. クライアントアプリケーションにAPIの認証情報を埋め込む必要がなくなる
2. 複数のクライアントで認証情報を共有できる
3. APIの呼び出し回数を制限・管理できる
4. クライアント側の実装を簡素化できる

### 前提条件

プロキシサーバーを構築するには以下が必要です：

1. JDK 11以上
2. Kotlin 2.1.21以上
3. 日本郵便デジタルアドレスAPIのアカウント（クライアントIDとシークレットキー）

### 環境変数の設定

プロキシサーバーは `.env` ファイルを使用して設定します。以下の環境変数を設定してください：

```
## 日本郵便APIの設定

# 本番環境やテスト環境のURLに変更してください
LATTE_ENDPOINT_URL=https://stb-ssss.da.pf.japanpost.jp  

# 日本郵便から提供されたクライアントID
LATTE_CLIENT_ID=your_client_id

# 日本郵便から提供されたシークレットキー
LATTE_SECRET_KEY=your_secret_key            

## プロキシサーバーの設定
# プロキシサーバーが使用するポート番号
LATTE_SERVER_PORT=12345                            

# トークン制限のリフィル期間（秒）
LATTE_TOKEN_REFILL_PERIOD=300

# 期間内に許可するAPI実行回数
LATTE_CALL_TOKEN_COUNT=60                          
```

### プロキシサーバーの起動

以下のコマンドでプロキシサーバーを起動します：

```bash
cd server
./gradlew run
```

サーバーが起動すると、設定したポートでリクエストを受け付けるようになります。

### クライアントからのプロキシの利用

プロキシサーバーを利用するには、クライアントを以下のように設定します：

```kotlin
// プロキシサーバーのURLを指定してクライアントを作成
val latte = Latte.of("http://localhost:12345")

// 通常通りAPIを利用
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

### プロキシサーバーのセキュリティ

プロキシサーバーはAPIキーによって保護されています：

- すべてのAPIエンドポイント（`/proxytoken`を除く）は認証のために`X-API-KEY`ヘッダーが必要です
- クライアントは`/proxytoken`エンドポイントを呼び出すことでAPIキーを取得できます
- DefaultCredentialsProviderは自動的にAPIキーの取得と更新を処理し、latteで提供しているプロキシサーバーと連携するように設計されています

カスタム認証を実装するには、独自のCredentialsProviderを実装することができます：

```kotlin
// カスタム認証プロバイダーを作成
class MyCredentialsProvider : CredentialsProvider {
    override suspend fun provide(host: String): Pair<String, String> {
        // ヘッダー名と値を返す
        return "X-API-KEY" to "あなたのAPIキー"
    }
}

// プロキシ接続で使用する
val latte = Latte.of(
    ConnectionInfo.Proxy("http://localhost:12345")
        .with(MyCredentialsProvider())
)
```


## ライセンス

このプロジェクトは Apache License 2.0 の下でライセンスされています。詳細は [LICENSE](LICENSE) ファイルを参照してください。

```
Copyright 2025 milkcocoa0902

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
