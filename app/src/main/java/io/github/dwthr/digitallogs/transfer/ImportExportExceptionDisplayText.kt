package io.github.dwthr.digitallogs.transfer

import android.content.Context
import io.github.dwthr.digitallogs.R

context(context: Context)
fun ImportExportHandlerException.getExceptionDisplayText(): String {
	return when(this) {
		is ImportExportHandlerException.Export.ExportDBCacheFileError -> context.getString(R.string.export_database_cache_fail)
		is ImportExportHandlerException.Export.NoDatabaseFoundForExport -> context.getString(R.string.export_db_not_exists)
		is ImportExportHandlerException.Generic.UnableToOpenPath -> context.getString(R.string.generic_unable_to_open_stream)
		is ImportExportHandlerException.Import.IncompleteBackup -> context.getString(R.string.import_incomplete_backup)
		is ImportExportHandlerException.Import.InvalidBackupVersion -> context.getString(R.string.import_backup_invalid_version)
		is ImportExportHandlerException.Import.MetadataParseError -> context.getString(R.string.import_unable_parse_backup_metadata)
		is ImportExportHandlerException.Import.PreferencesParseError -> context.getString(R.string.import_preferences_parse_failure)
		is ImportExportHandlerException.Import.PreferencesStoreError -> context.getString(R.string.import_backup_preferences_failed)
		is ImportExportHandlerException.Import.UnrecognizedBackupVersion -> context.getString(R.string.import_backup_unrecognized_version)
	}
}