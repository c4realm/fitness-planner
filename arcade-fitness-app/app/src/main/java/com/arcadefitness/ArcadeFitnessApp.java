package com.arcadefitness;

import android.app.Application;

import com.arcadefitness.data.remote.RetrofitClient;

/**
 * ArcadeFitnessApp.java
 *
 * Custom Application class — initialises RetrofitClient once at app startup
 * so every part of the app can call RetrofitClient.getApi() safely.
 *
 * ⚠️ You MUST register this in AndroidManifest.xml:
 *
 *     <application
 *         android:name=".ArcadeFitnessApp"
 *         ...>
 */
public class ArcadeFitnessApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialise Retrofit with application context.
        // This must run before any Activity or Service uses RetrofitClient.
        RetrofitClient.init(this);
    }
}