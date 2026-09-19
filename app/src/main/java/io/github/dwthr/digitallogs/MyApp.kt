package io.github.dwthr.digitallogs

import android.app.Application

class MyApp : Application() { //TODO: Reread Google documentation on manual DI

    companion object {
        lateinit var appModule: AppModule
    }

    override fun onCreate() {
        super.onCreate()
        appModule = AppModuleImpl(this)
    }

    /** If you want to scope dependencies to a specific viewmodel's lifetime
     * then you would create the AppModule in that viewmodel vs this file
     * and set it to null when the viewmodel is OnCleared()
     */
}