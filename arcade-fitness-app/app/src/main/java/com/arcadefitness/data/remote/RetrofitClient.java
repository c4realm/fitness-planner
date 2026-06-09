package com.arcadefitness.data.remote;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.arcadefitness.utils.AppConstants;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String TAG = "RetrofitClient";

    private static RetrofitClient instance;
    private final Retrofit retrofit;
    private Context appContext;

    // ─────────────────────────────────────────────────────────────────────────
    //  Private constructor — builds OkHttp + Retrofit once
    // ─────────────────────────────────────────────────────────────────────────
    private RetrofitClient(Context context) {
        this.appContext = context.getApplicationContext();

        // Logging interceptor — prints full request/response in Logcat
        // Filter by tag "OkHttp" in Logcat to see all API traffic
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message ->
                Log.d("OkHttp", message));
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Auth interceptor — attaches Bearer token from SharedPreferences to every request
        Interceptor authInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                SharedPreferences prefs = appContext.getSharedPreferences(
                        AppConstants.PREF_FILE, Context.MODE_PRIVATE);
                String token = prefs.getString(AppConstants.PREF_AUTH_TOKEN, "");

                Request original = chain.request();
                Request.Builder builder = original.newBuilder()
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json");

                if (!token.isEmpty()) {
                    builder.header("Authorization", "Bearer " + token);
                }

                return chain.proceed(builder.build());
            }
        };

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(AppConstants.TIMEOUT_CONNECT, TimeUnit.SECONDS)
                .readTimeout(AppConstants.TIMEOUT_READ, TimeUnit.SECONDS)
                .writeTimeout(AppConstants.TIMEOUT_WRITE, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)   // always last so it logs the final request
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(AppConstants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Log.d(TAG, "RetrofitClient initialised → " + AppConstants.BASE_URL);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  init — ensures the singleton is created with a Context
    // ─────────────────────────────────────────────────────────────────────────
    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Singleton getter — call getInstance(context) anywhere in the app
    // ─────────────────────────────────────────────────────────────────────────
    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context);
        }
        return instance;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Create an ApiService implementation
    // ─────────────────────────────────────────────────────────────────────────
    public ApiService getApiService() {
        return retrofit.create(ApiService.class);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Static convenience — returns the ApiService without needing the instance
    // ─────────────────────────────────────────────────────────────────────────
    public static ApiService getApi() {
        if (instance == null) {
            throw new IllegalStateException(
                    "RetrofitClient not initialised. Call init(context) first.");
        }
        return instance.getApiService();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Reset — call this on logout so the token is dropped on next build
    // ─────────────────────────────────────────────────────────────────────────
    public static synchronized void reset() {
        instance = null;
        Log.d(TAG, "RetrofitClient reset (token cleared)");
    }
}
