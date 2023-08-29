package ru.threedplatforma.javasample;

import android.app.Application;

import ru.threedplatforma.sdk.PlatformaSDK;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PlatformaSDK.init(this, "YOUR TOKEN HERE");
    }

}
