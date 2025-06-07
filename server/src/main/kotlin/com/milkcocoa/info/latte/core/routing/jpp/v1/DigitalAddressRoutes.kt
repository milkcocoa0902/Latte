package com.milkcocoa.info.latte.core.routing.jpp.v1

import com.milkcocoa.info.latte.Latte
import com.milkcocoa.info.latte.core.LatteException
import com.milkcocoa.info.latte.model.addresszip.AddressZipRequest
import com.milkcocoa.info.latte.model.searchcode.SearchCodeRequest
import com.milkcocoa.info.latte.model.token.TokenRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.util.toMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.decodeFromMap

@Resource("/api/v1")
class DigitalAddressRoutes{
    @Resource("j/token")
    class IssueToken(
        val parent: DigitalAddressRoutes = DigitalAddressRoutes()
    )

    @Resource("addresszip")
    class AddressZip(
        val parent: DigitalAddressRoutes = DigitalAddressRoutes()
    )

    @Resource("searchcode/{code}")
    class SearchCode(
        val parent: DigitalAddressRoutes = DigitalAddressRoutes(),
        val code: String
    )
}

@OptIn(ExperimentalSerializationApi::class)
fun Route.digitalAddressRoutes(
    latte: Latte
){
    get<DigitalAddressRoutes.SearchCode>{
        val token = call.request.headers.get(HttpHeaders.Authorization)?.removePrefix("Bearer ") ?: error("")
        val searchCodeRequest: SearchCodeRequest = Properties.decodeFromMap(call.request.queryParameters.toMap())

        runCatching {
            call.respond(latte.search(token, it.code, searchCodeRequest))
        }.getOrElse {
            when(it){
                is LatteException -> call.respond(
                    status = it.statusCode ?: HttpStatusCode.InternalServerError,
                    message = it
                )
                else -> call.respond(
                    status = HttpStatusCode.InternalServerError,
                    message = LatteException.Unknown(it)
                )
            }
        }
    }

    post<DigitalAddressRoutes.IssueToken> {
        runCatching {
            call.respond(latte.token())
        }.getOrElse {
            when(it){
                is LatteException -> call.respond(
                    status = it.statusCode ?: HttpStatusCode.InternalServerError,
                    message = it
                )
                else -> call.respond(
                    status = HttpStatusCode.InternalServerError,
                    message = LatteException.Unknown(it)
                )
            }
        }
    }

    post<DigitalAddressRoutes.AddressZip> {
        val body: AddressZipRequest = call.receive()
        val token = call.request.headers.get(HttpHeaders.Authorization)?.removePrefix("Bearer ") ?: error("")

        runCatching {
            call.respond(latte.addressZip(token, body))
        }.getOrElse {
            when(it){
                is LatteException -> call.respond(
                    status = it.statusCode ?: HttpStatusCode.InternalServerError,
                    message = it
                )
                else -> call.respond(
                    status = HttpStatusCode.InternalServerError,
                    message = LatteException.Unknown(it)
                )
            }
        }
    }
}