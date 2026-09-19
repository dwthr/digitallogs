package io.github.dwthr.digitallogs.transfer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import io.github.dwthr.digitallogs.MyApp
import io.github.dwthr.digitallogs.common.domain.useEntry
import io.github.dwthr.digitallogs.logs.data.database.AppDatabase
import io.github.dwthr.digitallogs.logs.domain.error.AppResult
import io.github.dwthr.digitallogs.logs.domain.error.BackupMetadataValidationError
import io.github.dwthr.digitallogs.logs.domain.repository.preferences.UserPreferencesState
import io.github.dwthr.digitallogs.transfer.BackupFileMetadata.Companion.validate
import io.github.dwthr.digitallogs.transfer.ImportExportHandlerException.Export.ExportDBCacheFileError
import io.github.dwthr.digitallogs.transfer.ImportExportHandlerException.Export.NoDatabaseFoundForExport
import io.github.dwthr.digitallogs.transfer.ImportExportHandlerException.Generic.UnableToOpenPath
import io.github.dwthr.digitallogs.transfer.ImportExportHandlerException.Import.IncompleteBackup
import io.github.dwthr.digitallogs.transfer.ImportExportHandlerException.Import.InvalidBackupVersion
import io.github.dwthr.digitallogs.transfer.ImportExportHandlerException.Import.MetadataParseError
import io.github.dwthr.digitallogs.transfer.ImportExportHandlerException.Import.PreferencesParseError
import io.github.dwthr.digitallogs.transfer.ImportExportHandlerException.Import.PreferencesStoreError
import io.github.dwthr.digitallogs.transfer.ImportExportHandlerException.Import.UnrecognizedBackupVersion
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.io.FileNotFoundException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Handles import and exporting app data from/to external storage using the Zip format.
 * The stored zip contains a metadata file indicating the version and each backup structure.
 * Each structure represents a general storage type, implementation type, and then the data file.
 * A hypothetical example could be: Database -> Sqlite/Room/MongoDB/etc. -> data
 */
//TODO: Preview of imported content. Have an option to merge instead of wiping app
class ImportExportDataHandler(val database: AppDatabase, val databaseFileName: String, val appContext: Context, val preferencesName: String) { //TODO: Look into FileChannel
	private val bufferSize = 256 * 1024
	//TODO: BufferedOutputStream?
	//TODO: Use different context?
	//TODO: Have better flow for error handling and file export
	@OptIn(ExperimentalSerializationApi::class)
	fun importData(importedContentUri: Uri) { //TODO: Input URI and use buffered reads //TODO: Have file store checksum and process if mismatched
		Log.i(this::class.simpleName, "Closing database")
		database.close()

		val importFile = try {
			appContext.contentResolver.openInputStream(importedContentUri) ?: throw UnableToOpenPath()
		} catch (e: FileNotFoundException) {
			throw UnableToOpenPath()
		}


		val dbFile = appContext.getDatabasePath(databaseFileName)
		val mediaDir = File("${appContext.dataDir}/Media")

		val zipInputStream = ZipInputStream(importFile) //TODO: Look into buffering

		zipInputStream.use { zipData ->
			var entry = zipData.nextEntry

			//Expected imports //TODO: Have this be a class or function?
			var hasMetadata = false
			var hasMedia = false
			var hasDatabase = false
			var hasPreferences = false

			while(entry != null) {
				when(entry.name) { //TODO: throw exception if these entries cannot be found
					"metadata" -> { //TODO: Check if this might read to EOF if corrupt and will stop db from loading
						hasMetadata = true

						val metadata = try {
							Json.decodeFromStream<BackupFileMetadata>(zipData) //TODO: Attempt conversion if not valid type here?
						} catch (e: Exception) {
							e.message?.let {
								Log.e(this::class.simpleName, it)
							}
							throw MetadataParseError()
						}
						when(val result = metadata.validate()) {
							is AppResult.Error -> {
								Log.w(this::class.simpleName, "Backup file version unrecognized")
								when (result.error) {
									BackupMetadataValidationError.UNKNOWN_VERSION -> {
										throw UnrecognizedBackupVersion()
									}
									BackupMetadataValidationError.INVALID_VERSION -> {
										throw InvalidBackupVersion()
									}
								}
							}
							is AppResult.Success -> Unit
						}
						Log.i(this::class.simpleName, "Imported backup metadata version: ${metadata.validate()}")
					}
					"database/room/data" -> {
						hasDatabase = true

						Log.v(this::class.simpleName, "Closing database")
							database.close()
						if (!appContext.deleteDatabase(databaseFileName) && dbFile.exists()) error("Failed to delete database")

						dbFile.outputStream().use { newDBStream ->
							zipData.copyTo(newDBStream, bufferSize)
						}
					}
					"preferences/datastore/data" -> { //FIXME: Silently fails when backup file is modified
						hasPreferences = true

						Log.v(this::class.simpleName, "Reading preferences file")
						val preferences = try {
							Json.decodeFromStream<UserPreferencesState>(zipData) //TODO: Map metadata version to datastore schema
						} catch (e: Exception) {
							e.message?.let {
								Log.e(this::class.simpleName, it)
							}
							throw PreferencesParseError()
						}
						Log.d(this::class.simpleName, "Preferences: $preferences")
						runBlocking { //TODO: Find best scope and show loading/handle error
							try {
								Log.v(this::class.simpleName, "Setting user preferences state")
								MyApp.appModule.userPreferencesRepository.setUserPreferencesState(preferences)
							} catch (e: Exception) {
								Log.e(this::class.simpleName, "Unable to set user preferences")
								throw PreferencesStoreError()
							}
						}
					}
					else -> {
						Log.d(this::class.simpleName, "${entry.name}")
						if (entry.name.startsWith("media/")) {
							hasMedia = true
							val mediaItem = File(mediaDir, entry.name.substringAfter("media/"))

							if (entry.isDirectory) {
								Log.d(this::class.simpleName, "Media found: ${mediaItem.name}")
								mediaItem.mkdirs()
							} else {
								mediaItem.parentFile?.mkdirs()
								mediaItem.outputStream().use { mediaOutput ->
									zipData.copyTo(mediaOutput, bufferSize)
								}
								Log.v(this::class.simpleName, "Imported media")
							}
						}
					}
				}

				zipData.closeEntry()
				entry = zipData.nextEntry
			}
			if (!hasMedia) {
				Log.i(this::class.simpleName, "Missing media")
			}
			if (!(hasMetadata && hasDatabase && hasPreferences)) {
				Log.e(this::class.simpleName, "Incomplete backup")
				if (!hasMetadata) Log.e(this::class.simpleName, "Missing metadata")
				if (!hasDatabase) Log.e(this::class.simpleName, "Missing database")
				if (!hasPreferences) Log.e(this::class.simpleName, "Missing preferences")
				throw IncompleteBackup()
			}
		}

		val packageManager = appContext.packageManager
		val mainActivityIntent = packageManager.getLaunchIntentForPackage(appContext.packageName)

		mainActivityIntent?.let {
			val restartIntent = Intent.makeRestartActivityTask(it.component)
			Log.v(this::class.simpleName, "Backup success: asking system to restart app after exit")
			appContext.startActivity(restartIntent)

			Log.v(this::class.simpleName, "Closing current session")
			Runtime.getRuntime().exit(0)
		}
	}

	fun exportData(exportContentUri: Uri, preferencesStateExport: UserPreferencesState) {

		val databaseFile = appContext.getDatabasePath(databaseFileName)
		if (!databaseFile.exists()) throw NoDatabaseFoundForExport()

		val exportOutput = appContext.contentResolver.openOutputStream(exportContentUri) ?: throw UnableToOpenPath()
		val zipOutputStream = ZipOutputStream(exportOutput) //TODO: Look into buffering

		try {
			zipOutputStream.use { zippedOutput ->
				val metadataEntry = ZipEntry("metadata")
				val metadataJsonData = Json.encodeToString(BackupFileMetadata())

				zippedOutput.useEntry(metadataEntry) {
					it.write(metadataJsonData.toByteArray())
				}

				val mediaDir = File("${appContext.dataDir}/Media")
				if (mediaDir.exists()) {
					val mediaBaseEntry = "media"

					mediaDir.listFiles { it.isDirectory }
						?.forEach { folder ->
							Log.d(this::class.simpleName, "Exporting media: ${folder.name}")
							folder.listFiles { it.isFile }?.forEach { file ->
								val fileEntry = ZipEntry("$mediaBaseEntry/${folder.name}/${file.name}")
								file.inputStream().use { fis ->
									zippedOutput.useEntry(fileEntry) { mediaOutput ->
										fis.copyTo(mediaOutput, bufferSize)
									}
								}
							}
						}
				}

				val cachedDbFile = File(appContext.cacheDir, "${databaseFileName}_backup.db")
				if (cachedDbFile.exists() && !cachedDbFile.delete()) throw ExportDBCacheFileError()

				database.openHelper.writableDatabase.execSQL("VACUUM INTO '${cachedDbFile.absolutePath}'")
				val dbEntry = ZipEntry("database/room/data").apply {
					method = ZipEntry.DEFLATED
				}
				zippedOutput.useEntry(dbEntry) { zipOutput -> //TODO: Look into channels
					cachedDbFile.inputStream().use { dbStream ->
						dbStream.copyTo(zipOutput, bufferSize)
					}

					val datastoreEntry = ZipEntry("preferences/datastore/data")
					preferencesStateExport.let { preferencesData ->
						val preferencesJsonData = Json.encodeToString(preferencesData)

						zippedOutput.useEntry(datastoreEntry) {
							it.write(preferencesJsonData.toByteArray())
						}
					}
				}

				println("Deleting cached db copy")
				cachedDbFile.delete()
			}
		} catch (e: Exception) {
			println("Backup failed")
			println("Cleaning up files")
			DocumentsContract.deleteDocument(appContext.contentResolver, exportContentUri)
			throw e
		}
	}
//			database.close()
	//		databaseFile.inputStream().use { dbData ->
	//			CheckedInputStream(
	//				dbData,
	//				exportChecksum //TODO: When minAPI >= 34, Exclusively set export to CRC32C
	//			).use { checkedDbData ->
	//				exportLocation.use { exportStream ->
	//					checkedDbData.copyTo(exportStream)
	//				} //TODO: Works?
	//			}
	//		}
	//		//TODO: reopen db
}