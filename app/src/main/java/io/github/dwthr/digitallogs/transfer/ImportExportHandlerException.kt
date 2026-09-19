package io.github.dwthr.digitallogs.transfer

sealed interface ImportExportHandlerException {
	sealed interface Generic: ImportExportHandlerException, Export, Import {
		class UnableToOpenPath: Exception(), Generic
	}
	sealed interface Import: ImportExportHandlerException {
		class InvalidBackupVersion: IllegalStateException(), Import
		class UnrecognizedBackupVersion: IllegalStateException(), Import
		class MetadataParseError: Exception(), Import
		class PreferencesParseError: Exception(), Import
		class PreferencesStoreError: Exception(), Import
		class IncompleteBackup: Exception(), Import
	}

	sealed interface Export: ImportExportHandlerException {
		class ExportDBCacheFileError: Exception(), Export
		class NoDatabaseFoundForExport: Exception(), Export
	}
}