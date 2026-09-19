package io.github.dwthr.digitallogs

import android.content.Context
import androidx.room.Room
import io.github.dwthr.digitallogs.audio.AppAudioHandler
import io.github.dwthr.digitallogs.audio.data.AndroidAudioRecorderImpl
import io.github.dwthr.digitallogs.audio.data.AppAudioHandlerImpl
import io.github.dwthr.digitallogs.logs.data.Preferences
import io.github.dwthr.digitallogs.logs.data.database.AppDatabase
import io.github.dwthr.digitallogs.logs.data.repository.LocalRepositoryImpl
import io.github.dwthr.digitallogs.logs.data.repository.UserPreferencesRepositoryImpl
import io.github.dwthr.digitallogs.logs.domain.repository.LocalRepository
import io.github.dwthr.digitallogs.logs.domain.repository.preferences.UserPreferencesRepository
import io.github.dwthr.digitallogs.transfer.ImportExportDataHandler
import kotlinx.coroutines.Dispatchers

class AppModuleImpl(
    private val appContext: Context
) : AppModule { //FIXME: Avoid android dependencies from module
    override val appDatabase: AppDatabase by lazy { // TODO : Hilt?
        Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            AppDatabase.DB_NAME
        ).build()//.addMigrations(MIGRATION_1_2).build()
    }
    override val importExportDataHandler: ImportExportDataHandler by lazy {
        ImportExportDataHandler(
	        database = appDatabase,
	        databaseFileName = AppDatabase.DB_NAME,
	        appContext = appContext,
	        preferencesName = Preferences.PREFS_NAME
        )
    }
    override val localRepository: LocalRepository by lazy {
        LocalRepositoryImpl(
	        categoryDao = appDatabase.categoryDao,
	        logDao = appDatabase.logDao,
	        tagDao = appDatabase.tagDao,
	        entryDao = appDatabase.entryDao,
			defaultDispatcher = Dispatchers.Default //TODO: Best dispatcher?
        )
    }
    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepositoryImpl(
            dataStore = appContext.dataStore
        )
    }
    override val appAudioHandler: AppAudioHandler by lazy {
        AppAudioHandlerImpl(
	        appContext = appContext,
	        audioRecorder = audioRecorder,
	        localRepository = localRepository
        )
    }
    override val audioRecorder: AndroidAudioRecorderImpl by lazy {
        AndroidAudioRecorderImpl(appContext)
    }
}