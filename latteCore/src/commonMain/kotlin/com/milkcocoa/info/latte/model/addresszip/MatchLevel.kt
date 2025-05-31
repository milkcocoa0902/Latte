package com.milkcocoa.info.latte.model.addresszip

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = MatchLevel.Companion::class)
@SerialName("MatchLevel")
enum class MatchLevel{
    Prefecture,
    City,
    Town;

    companion object: KSerializer<MatchLevel> {
        override val descriptor: SerialDescriptor
            get() = PrimitiveSerialDescriptor(
                serialName = "MatchLevel",
                kind = PrimitiveKind.INT
            )

        override fun deserialize(decoder: Decoder): MatchLevel {
            val a = decoder.decodeInt()
            return entries.find { it.ordinal + 1 == a } ?: error("")
        }

        override fun serialize(encoder: Encoder, value: MatchLevel) {
            encoder.encodeInt(value.ordinal + 1)
        }
    }
}