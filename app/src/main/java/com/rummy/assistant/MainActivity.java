package com.rummy.assistant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rummy.assistant.vision.CardRecognition;
import com.rummy.assistant.vision.ScreenCaptureManager;
import com.rummy.assistant.model.Card;
import com.rummy.assistant.model.HandManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btnStartAssistant, btnStopAssistant, btnStartScreenshot, btnManualEntry;
    private TextView txtOverlayStatus, txtAccessibilityStatus, txtCardCount, txtHandValidity;
    private ScreenCaptureManager screenCaptureManager;
    private CardRecognition cardRecognition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        screenCaptureManager = new ScreenCaptureManager(this);
        cardRecognition = new CardRecognition();

        btnStartAssistant = findViewById(R.id.btnStartAssistant);
        btnStopAssistant = findViewById(R.id.btnStopAssistant);
        btnStartScreenshot = findViewById(R.id.btnStartScreenshot);
        btnManualEntry = findViewById(R.id.btnManualEntry);
        Button btnOpenAccessibilitySettings = findViewById(R.id.btnOpenAccessibilitySettings);

        txtOverlayStatus = findViewById(R.id.txtOverlayStatus);
        txtAccessibilityStatus = findViewById(R.id.txtAccessibilityStatus);
        txtCardCount = findViewById(R.id.txtCardCount);
        txtHandValidity = findViewById(R.id.txtHandValidity);

        btnStartAssistant.setOnClickListener(v -> startAssistant());
        btnStopAssistant.setOnClickListener(v -> stopAssistant());
        btnOpenAccessibilitySettings.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
        btnManualEntry.setOnClickListener(v -> openManualEntry());
        btnStartScreenshot.setOnClickListener(v -> startScreenshotRecognition());

        updateStatuses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatuses();
    }

    private void updateStatuses() {
        boolean hasOverlayPermission = Settings.canDrawOverlays(this);
        txtOverlayStatus.setText("Overlay Permission: " + (hasOverlayPermission ? "Granted" : "Denied"));

        txtAccessibilityStatus.setText("Accessibility Service: Check Settings");
    }

    private void startAssistant() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 1234);
            Toast.makeText(this, "Please grant overlay permission", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, FloatingWindowService.class);
        startForegroundService(intent);
        Toast.makeText(this, "Assistant Started", Toast.LENGTH_SHORT).show();
    }

    private void stopAssistant() {
        Intent intent = new Intent(this, FloatingWindowService.class);
        stopService(intent);
        Toast.makeText(this, "Assistant Stopped", Toast.LENGTH_SHORT).show();
    }

    private void openManualEntry() {
        Intent intent = new Intent(this, ManualEntryActivity.class);
        startActivity(intent);
    }

    private void startScreenshotRecognition() {
        Intent intent = screenCaptureManager.getProjectionIntent();
        startActivityForResult(intent, 2000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2000) {
            if (resultCode == RESULT_OK && data != null) {
                screenCaptureManager.startCapture(resultCode, data, new ScreenCaptureManager.ScreenCaptureListener() {
                    @Override
                    public void onFrameCaptured(android.graphics.Bitmap bitmap) {
                        List<CardRecognition.RecognitionResult> results = cardRecognition.processScreenshot(bitmap);
                        List<Card> cards = results.stream()
                                .map(r -> r.card)
                                .collect(java.util.stream.Collectors.toList());

                        HandManager.getInstance().setHand(cards);
                        Toast.makeText(MainActivity.this, "Captured " + cards.size() + " cards", Toast.LENGTH_SHORT).show();
                        screenCaptureManager.stopCapture();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Screenshot permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 1234) {
            updateStatuses();
        }
    }
}
