package com.rummy.assistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import com.rummy.assistant.engine.ArrangementComparator;
import com.rummy.assistant.engine.CardGroup;
import com.rummy.assistant.engine.CardSorter;
import com.rummy.assistant.engine.HandArrangement;
import com.rummy.assistant.model.Card;
import com.rummy.assistant.model.HandManager;
import com.rummy.assistant.model.JokerConfig;
import com.rummy.assistant.model.Rank;

import java.util.List;
import java.util.stream.Collectors;

import java.util.List;
import java.util.stream.Collectors;

public class FloatingWindowService extends Service implements HandManager.HandChangeListener {
    private static final String CHANNEL_ID = "FloatingAssistantChannel";
    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;
    private TextView txtSortedHand;
    private View comparisonView;
    private ArrangementComparator comparator = new ArrangementComparator();

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        initFloatingWindow();
        HandManager.getInstance().addListener(this);
    }

    private void initFloatingWindow() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        floatingView = layoutInflater.inflate(R.layout.layout_floating_overlay, null);

        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 100;

        windowManager.addView(floatingView, params);
        setupOverlayControls();
    }

    private void setupOverlayControls() {
        LinearLayout header = floatingView.findViewById(R.id.overlay_header);
        Button btnClose = floatingView.findViewById(R.id.btnClose);
        Button btnMinimize = floatingView.findViewById(R.id.btnMinimize);
        Button btnOpenMain = floatingView.findViewById(R.id.btnOpenMain);
        Button btnInspect = floatingView.findViewById(R.id.btnInspectArrangements);
        txtSortedHand = floatingView.findViewById(R.id.txtSortedHand);

        btnClose.setOnClickListener(v -> stopSelf());

        btnMinimize.setOnClickListener(v -> {
            View content = floatingView.findViewById(R.id.txtSortedHand);
            if (content.getVisibility() == View.VISIBLE) {
                content.setVisibility(View.GONE);
                ((LinearLayout) floatingView).getChildAt(2).setVisibility(View.GONE);
            } else {
                content.setVisibility(View.VISIBLE);
                ((LinearLayout) floatingView).getChildAt(2).setVisibility(View.VISIBLE);
            }
        });

        btnOpenMain.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        btnInspect.setOnClickListener(v -> showComparison());

        header.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    private void showComparison() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        comparisonView = inflater.inflate(R.layout.layout_comparison_view, null);

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.x = 0;
        params.y = 100;

        windowManager.removeView(floatingView);
        windowManager.addView(comparisonView, params);
        setupComparisonView();
    }

    private void setupComparisonView() {
        TextView txtScenario = comparisonView.findViewById(R.id.txtScenario);
        TextView txtCurrent = comparisonView.findViewById(R.id.txtCurrentArrangement);
        TextView txtCurrentPts = comparisonView.findViewById(R.id.txtCurrentPoints);
        TextView txtAlt = comparisonView.findViewById(R.id.txtAltArrangement);
        TextView txtAltPts = comparisonView.findViewById(R.id.txtAltPoints);
        Button btnClose = comparisonView.findViewById(R.id.btnCloseComparison);

        List<Card> hand = HandManager.getInstance().getHand();
        if (hand.isEmpty()) {
            txtScenario.setText("No hand detected");
        } else {
            // Simulate "Discard first card" scenario for demonstration
            Card selectedCard = hand.get(0);
            ArrangementComparator.ComparisonResult result = comparator.compare(
                    hand, selectedCard, null, ArrangementComparator.Scenario.SIMULATE_DISCARD, new JokerConfig(Rank.TWO)
            );

            txtScenario.setText("Scenario: " + result.scenarioDescription);
            txtCurrent.setText(formatArrangement(result.current));
            txtCurrentPts.setText("Points: " + result.current.getTotalPoints());
            txtAlt.setText(formatArrangement(result.alternative));
            txtAltPts.setText("Points: " + result.alternative.getTotalPoints());
        }

        btnClose.setOnClickListener(v -> {
            windowManager.removeView(comparisonView);
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 100;
            params.y = 100;
            windowManager.addView(floatingView, params);
        });
    }

    private String formatArrangement(HandArrangement arrangement) {
        if (arrangement == null) return "No arrangement";
        StringBuilder sb = new StringBuilder();
        for (CardGroup g : arrangement.getGroups()) {
            sb.append(g.toString()).append("\n");
        }
        sb.append("Deadwood: ").append(arrangement.getDeadwood().size()).append(" cards");
        return sb.toString();
    }

    @Override
    public void onHandChanged(List<Card> newHand) {
        if (txtSortedHand != null) {
            if (newHand.isEmpty()) {
                txtSortedHand.setText("No cards detected");
            } else {
                // Apply simple sorting by default for the compact view
                List<Card> sorted = CardSorter.sort(newHand, CardSorter.SortMode.SIMPLE);
                String handText = sorted.stream()
                        .map(Card::toString)
                        .collect(Collectors.joining(", "));
                txtSortedHand.setText("Hand: " + handText);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        HandManager.getInstance().removeListener(this);
        if (comparisonView != null) {
            windowManager.removeView(comparisonView);
        }
        if (floatingView != null) {
            windowManager.removeView(floatingView);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Floating Assistant Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
