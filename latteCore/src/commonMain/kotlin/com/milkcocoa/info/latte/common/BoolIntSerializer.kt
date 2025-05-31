package com.milkcocoa.info.latte.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object BoolIntSerializer: KSerializer<Boolean> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(
            serialName = "BoolInt",
            kind = PrimitiveKind.INT
        )

    override fun deserialize(decoder: Decoder): Boolean {
        return decoder.decodeInt() != 0
    }

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeInt(if(value) 1 else 0)
    }
}