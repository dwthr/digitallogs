package io.github.dwthr.digitallogs.logs.domain.error

sealed interface DataError: AppError {
	enum class Database: DataError {
		DISK_FULL,
//		CONFLICT,
//		INVALID_FOREIGN_KEY,
//		UNKNOWN
	}
	enum class Network: DataError
}

sealed interface EntityError: DataError {
	enum class Title: EntityError {
		IS_BLANK,
		TOO_LONG,
		CONFLICT,
	}
}

enum class BackupMetadataValidationError: AppError { //FIXME: Does not belong in logs package
	UNKNOWN_VERSION, //Higher than current app version
	INVALID_VERSION, //Negative or otherwise illegal version
}

sealed interface AddTagToCategoryError: AppError {
	data class Tag(val error: DataError): AddTagToCategoryError
	data class Category(val error: DataError): AddTagToCategoryError
}
sealed interface AddTagToLogError: AppError {
	data class Tag(val error: DataError): AddTagToLogError
	data class Log(val error: DataError): AddTagToLogError
}