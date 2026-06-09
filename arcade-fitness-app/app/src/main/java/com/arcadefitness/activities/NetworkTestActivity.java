package com.arcadefitness.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.arcadefitness.R;
import com.arcadefitness.data.remote.ApiService;
import com.arcadefitness.data.remote.RetrofitClient;
import com.arcadefitness.network.NetworkChangeReceiver;
import com.arcadefitness.utils.AppConstants;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * NetworkTestActivity
 *
 * Drop-in debug screen to verify API connectivity.
 * Add this to AndroidManifest.xml temporarily:
 *
 *   <activity android:name=".activities.NetworkTestActivity" android:exported="true"/>
 *
 * Then either:
 *   a) Launch it directly from adb:
 *      adb shell am start -n com.arcadefitness/.activities.NetworkTestActivity
 *   b) Or temporarily set it as LAUNCHER in the manifest while debugging.
 *
 * DELETE or remove from manifest before submitting.
 */
public class NetworkTestActivity extends AppCompatActivity {

    private static final String TAG = "NetworkTest";
    private TextView tvStatus;
    private TextView tvUrl;
    private Button btnTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inline layout — no XML needed for this debug screen
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 80, 48, 48);

        tvUrl = new TextView(this);
        tvUrl.setTextSize(13);
        tvUrl.setPadding(0, 0, 0, 24);
        tvUrl.setText("Target: " + AppConstants.BASE_URL);
        layout.addView(tvUrl);

        btnTest = new Button(this);
        btnTest.setText("Test connection");
        btnTest.setOnClickListener(v -> runTest());
        layout.addView(btnTest);

        tvStatus = new TextView(this);
        tvStatus.setTextSize(15);
        tvStatus.setPadding(0, 32, 0, 0);
        tvStatus.setText("Tap the button to test");
        layout.addView(tvStatus);

        setContentView(layout);
    }

    private void runTest() {
        btnTest.setEnabled(false);
        tvStatus.setText("Testing...");
        tvStatus.setTextColor(0xFF888888);

        // Step 1: check local network state
        boolean localOk = NetworkChangeReceiver.isConnected(this);
        if (!localOk) {
            show("✗ No network connection detected on device.\nConnect to Wi-Fi or mobile data first.", false);
            btnTest.setEnabled(true);
            return;
        }

        // Step 2: hit the health check endpoint
        ApiService api = RetrofitClient.getInstance(this).getApiService();
        Call<ResponseBody> call = api.healthCheck();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnTest.setEnabled(true);
                if (response.isSuccessful()) {
                    show("✓ Connected!  HTTP " + response.code() +
                            "\n\nServer is reachable at:\n" + AppConstants.BASE_URL +
                            "\n\nSync queue is ready to flush.", true);
                    Log.d(TAG, "Health check OK: " + response.code());
                } else {
                    show("✗ Server responded but returned HTTP " + response.code() +
                            "\n\nMake sure your server has a GET /api/health endpoint that returns 200.", false);
                    Log.w(TAG, "Health check non-200: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnTest.setEnabled(true);
                String msg = t.getMessage() != null ? t.getMessage() : t.toString();

                String hint = "";
                if (msg.contains("CLEARTEXT")) {
                    hint = "\n\nFIX: network_security_config.xml is missing or your IP is not listed in it.";
                } else if (msg.contains("refused") || msg.contains("connect")) {
                    hint = "\n\nFIX: Check the IP and port in AppConstants.java.\nMake sure your server is running.";
                } else if (msg.contains("timeout")) {
                    hint = "\n\nFIX: Phone and PC may be on different networks, or a firewall is blocking the port.";
                } else if (msg.contains("Unable to resolve host")) {
                    hint = "\n\nFIX: Use a raw IP address (e.g. 192.168.1.x), not a hostname.";
                }

                show("✗ Failed: " + msg + hint, false);
                Log.e(TAG, "Health check failed: " + msg, t);
            }
        });
    }

    private void show(String message, boolean success) {
        tvStatus.setText(message);
        tvStatus.setTextColor(success ? 0xFF1D9E75 : 0xFFD85A30);
    }
}