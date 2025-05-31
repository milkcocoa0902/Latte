package com.milkcocoa.info.latte.model.searchcode

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = ChoikiType.Companion::class)
@SerialName("ChoikiType")
enum class ChoikiType{
    WithoutBrackets,
    WithBrackets;

    companion object: KSerializer<ChoikiType> {
        override val descriptor: SerialDescriptor
            get() = PrimitiveSerialDescriptor("ChoikiType", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): ChoikiType {
            val a = decoder.decodeInt()
            return entries.find { it.ordinal + 1 == a } ?: error("")
        }

        override fun serialize(encoder: Encoder, value: ChoikiType) {
            encoder.encodeInt(value.ordinal + 1)
        }
    }
}