package io.github.dwthr.digitallogs.logs.domain.serializers

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import io.github.dwthr.digitallogs.logs.domain.repository.preferences.UserPreferencesStateDefaults
import io.github.dwthr.digitallogs.proto.AppPreferences
import java.io.InputStream
import java.io.OutputStream

object AppPreferencesSerializer: Serializer<AppPreferences> { //TODO: Move to ./common/domain and have separate ./util/*?
	override val defaultValue: AppPreferences = AppPreferences.newBuilder()
		.setAllowScreenshots(UserPreferencesStateDefaults.state.allowScreenshots)
		.setDestructiveCooldownEnabled(UserPreferencesStateDefaults.state.destructiveCooldownEnabled)
		.buildPartial()

	override suspend fun readFrom(input: InputStream): AppPreferences {
		try {
			return AppPreferences.parseFrom(input)
		} catch (exception: InvalidProtocolBufferException) {
			throw CorruptionException("Cannot read proto.", exception)
		}
	}

	override suspend fun writeTo(t: AppPreferences, output: OutputStream) {
		return t.writeTo(output)
	}
}