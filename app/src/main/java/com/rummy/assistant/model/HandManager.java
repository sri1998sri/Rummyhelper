package com.rummy.assistant.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HandManager {
    private static HandManager instance;
    private final List<Card> currentHand = new CopyOnWriteArrayList<>();
    private final List<HandChangeListener> listeners = new CopyOnWriteArrayList<>();

    public interface HandChangeListener {
        void onHandChanged(List<Card> newHand);
    }

    public static synchronized HandManager getInstance() {
        if (instance == null) {
            instance = new HandManager();
        }
        return instance;
    }

    public void setHand(List<Card> hand) {
        currentHand.clear();
        currentHand.addAll(hand);
        notifyListeners();
    }

    public void addCard(Card card) {
        if (!currentHand.contains(card)) {
            currentHand.add(card);
            notifyListeners();
        }
    }

    public List<Card> getHand() {
        return Collections.unmodifiableList(currentHand);
    }

    public void clearHand() {
        currentHand.clear();
        notifyListeners();
    }

    public void addListener(HandChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(HandChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        List<Card> handCopy = new ArrayList<>(currentHand);
        for (HandChangeListener listener : listeners) {
            listener.onHandChanged(handCopy);
        }
    }
}
