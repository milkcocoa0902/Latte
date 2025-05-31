package com.milkcocoa.info.latte.model.searchcode

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = ActualSearchType.Companion::class)
@SerialName("ActualSearchType")
enum class ActualSearchType{
    DGACode,
    ZipCode,
    BizZipCode;

    companion object: KSerializer<ActualSearchType> {
        override val descriptor = PrimitiveSerialDescriptor(
            "ActualSearchType",
            PrimitiveKind.STRING
        )

        override fun deserialize(decoder: Decoder): ActualSearchType{
            val a = decoder.decodeString()
            return entries.find { it.name.lowercase() == a } ?: error("")
        }

        override fun serialize(encoder: Encoder, value: ActualSearchType) =
            encoder.encodeString(
                value.name.lowercase()
            )
    }
}