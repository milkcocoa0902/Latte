package com.milkcocoa.info.latte.core

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

actual fun ktorHttpClient(): HttpClient {
    return HttpClient(OkHttp){
        install(ContentNegotiation){
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = false
            })
        }
    }
}