package io.github.dwthr.digitallogs.common.domain

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

object InstantSerializer: KSerializer<Instant> {
	private val delegate = Long.serializer()

	override val descriptor = delegate.descriptor

	override fun serialize(encoder: Encoder, value: Instant) {
		delegate.serialize(encoder, value.toEpochMilliseconds())
	}

	override fun deserialize(decoder: Decoder): Instant {
		return Instant.fromEpochMilliseconds(delegate.deserialize(decoder))
	}
}