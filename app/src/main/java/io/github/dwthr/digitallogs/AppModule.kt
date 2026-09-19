package io.github.dwthr.digitallogs

import io.github.dwthr.digitallogs.audio.AppAudioHandler
import io.github.dwthr.digitallogs.audio.data.AndroidAudioRecorderImpl
import io.github.dwthr.digitallogs.logs.data.database.AppDatabase
import io.github.dwthr.digitallogs.logs.domain.repository.LocalRepository
import io.github.dwthr.digitallogs.logs.domain.repository.preferences.UserPreferencesRepository
import io.github.dwthr.digitallogs.transfer.ImportExportDataHandler

interface AppModule {
    val appDatabase: AppDatabase
    val importExportDataHandler: ImportExportDataHandler
    val localRepository: LocalRepository
    val userPreferencesRepository: UserPreferencesRepository
    val appAudioHandler: AppAudioHandler
    val audioRecorder: AndroidAudioRecorderImpl
}