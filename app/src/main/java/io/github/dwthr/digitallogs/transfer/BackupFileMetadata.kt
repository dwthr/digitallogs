package io.github.dwthr.digitallogs.transfer

import io.github.dwthr.digitallogs.logs.domain.error.AppResult
import io.github.dwthr.digitallogs.logs.domain.error.BackupMetadataValidationError
import kotlinx.serialization.Serializable

@Serializable
data class BackupFileMetadata(
	val backupVersion: Int = CURRENT_BACKUP_VERSION //TODO: Change implementation
) {
	companion object{
		const val CURRENT_BACKUP_VERSION = 1
		fun BackupFileMetadata.validate(): AppResult<BackupFileMetadata, BackupMetadataValidationError> { //TODO: Move to separate class?
			return when {
				this.backupVersion <= 0 -> AppResult.Error(BackupMetadataValidationError.INVALID_VERSION)
				this.backupVersion == CURRENT_BACKUP_VERSION -> AppResult.Success(this)
				else -> AppResult.Success(this) //TODO: Convert
			}
		}
	}
}
