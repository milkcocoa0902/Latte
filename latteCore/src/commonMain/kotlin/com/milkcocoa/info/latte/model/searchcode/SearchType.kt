package com.milkcocoa.info.latte.model.searchcode

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = SearchType.Companion::class)
@SerialName("SearchType")
enum class SearchType{
    WithBiz,
    WithoutBiz;

    companion object: KSerializer<SearchType> {
        override val descriptor: SerialDescriptor
            get() = PrimitiveSerialDescriptor("SearchType", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): SearchType {
            val a = decoder.decodeInt()
            return entries.find { it.ordinal + 1 == a } ?: error("")
        }

        override fun serialize(encoder: Encoder, value: SearchType) {
            encoder.encodeInt(value.ordinal + 1)
        }
    }
}