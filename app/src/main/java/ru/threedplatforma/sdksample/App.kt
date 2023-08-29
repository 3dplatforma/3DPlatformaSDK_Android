package ru.threedplatforma.sdksample

import android.app.Application
import ru.threedplatforma.sdk.PlatformaSDK

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        PlatformaSDK.init(
            this,
            "YOUR TOKEN HERE"
        )
    }

}