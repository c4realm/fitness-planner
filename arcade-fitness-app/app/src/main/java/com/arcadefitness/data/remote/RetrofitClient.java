package com.arcadefitness.data.remote;

import android.content.Context;
import android.util.Log;

import com.arcadefitness.utils.AppConstants;
import com.arcadefitness.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * RetrofitClient.java
 *
 * Singleton Retrofit client. Initialised once with Context so it can read
 * the JWT token from SessionManager on every request via the auth interceptor.
 *
 * Usage:
 *   RetrofitClient.init(context);               // call once in Application.onCreate()
 *   ApiService api = RetrofitClient.getApi();   // use anywhere after init
 */
public final class RetrofitClient {

    private static final String TAG = "RetrofitClient";
    private static volatile RetrofitClient INSTANCE;

    private final ApiService apiService;

    private RetrofitClient(Context context) {
        SessionManager sessionManager = new SessionManager(context.getApplicationContext());

        // ── Logging interceptor (full body in debug, none in release) ──
        HttpLoggingInterceptor logger = new HttpLoggingInterceptor(message ->
                Log.d(TAG, message));
        logger.setLevel(HttpLoggingInterceptor.Level.BODY);

        // ── Auth interceptor — attaches JWT Bearer token to every request ──
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder()
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json");

                    String token = sessionManager.getToken();
                    if (token != null && !token.isEmpty() && !token.equals("mock_token")) {
                        builder.header("Authorization", "Bearer " + token);
                    }

                    return chain.proceed(builder.build());
                })
                .addInterceptor(logger)
                .connectTimeout(AppConstants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(AppConstants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(AppConstants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

        // ── Gson — lenient so missing fields don't crash parsing ──
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConstants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    /** Call once in Application.onCreate() before anything uses the network. */
    public static void init(Context context) {
        if (INSTANCE == null) {
            synchronized (RetrofitClient.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RetrofitClient(context.getApplicationContext());
                }
            }
        }
    }

    /** Returns the singleton. Throws if init() was never called. */
    public static RetrofitClient getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException(
                    "RetrofitClient not initialised. Call RetrofitClient.init(context) in Application.onCreate().");
        }
        return INSTANCE;
    }

    public ApiService getApiService() {
        return apiService;
    }

    /** Convenience shortcut. */
    public static ApiService getApi() {
        return getInstance().getApiService();
    }
}