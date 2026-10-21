package com.rummy.assistant;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.rummy.assistant.model.Card;
import com.rummy.assistant.model.HandManager;
import com.rummy.assistant.parser.CardParser;

import java.util.ArrayList;
import java.util.List;

public class CardAccessibilityService extends AccessibilityService {
    private static final String TAG = "CardAccessibilityService";
    private long lastScanTime = 0;
    private static final long SCAN_INTERVAL_MS = 1000; // Throttle to 1 second

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
            event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastScanTime < SCAN_INTERVAL_MS) {
                return; // Throttle
            }
            lastScanTime = currentTime;

            scanForCards();
        }
    }

    private void scanForCards() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        List<Card> detectedCards = new ArrayList<>();
        findCardsRecursive(rootNode, detectedCards);

        if (!detectedCards.isEmpty()) {
            Log.d(TAG, "Detected " + detectedCards.size() + " cards: " + detectedCards);
            HandManager.getInstance().setHand(detectedCards);
        }

        rootNode.recycle();
    }

    private void findCardsRecursive(AccessibilityNodeInfo node, List<Card> detectedCards) {
        if (node == null) return;

        // Check text
        if (node.getText() != null) {
            Card card = CardParser.parse(node.getText().toString());
            if (card != null) {
                detectedCards.add(card);
            }
        }

        // Check content description
        if (node.getContentDescription() != null) {
            Card card = CardParser.parse(node.getContentDescription().toString());
            if (card != null) {
                detectedCards.add(card);
            }
        }

        // Recurse to children
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                findCardsRecursive(child, detectedCards);
                child.recycle();
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Accessibility Service interrupted");
    }
}
