package com.rummy.assistant.engine;

import com.rummy.assistant.model.Card;
import com.rummy.assistant.model.JokerConfig;

import java.util.*;

public class RummySolver {
    private final JokerConfig jokerConfig;
    private HandArrangement bestArrangement;

    public RummySolver(JokerConfig jokerConfig) {
        this.jokerConfig = jokerConfig;
    }

    public HandArrangement solve(List<Card> hand) {
        bestArrangement = null;
        List<CardGroup> allPossibleGroups = generateAllPossibleGroups(hand);
        backtrack(hand, allPossibleGroups, new ArrayList<>(), new boolean[hand.size()], 0);
        return bestArrangement;
    }

    private void backtrack(List<Card> hand, List<CardGroup> possibleGroups, List<CardGroup> currentGroups, boolean[] used, int groupIdx) {
        // Calculate deadwood and check validity
        List<Card> deadwood = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (!used[i]) deadwood.add(hand.get(i));
        }

        int currentPoints = calculatePoints(deadwood);
        boolean isValid = checkValidity(currentGroups);

        if (bestArrangement == null || (isValid && (bestArrangement == null || !bestArrangement.isValid() || currentPoints < bestArrangement.getTotalPoints()))) {
            bestArrangement = new HandArrangement(new ArrayList<>(currentGroups), deadwood, currentPoints, isValid);
        }

        // Try adding more groups
        for (int i = groupIdx; i < possibleGroups.size(); i++) {
            CardGroup group = possibleGroups.get(i);
            if (canAddGroup(group, hand, used)) {
                markUsed(group, hand, used, true);
                currentGroups.add(group);

                backtrack(hand, possibleGroups, currentGroups, used, i + 1);

                currentGroups.remove(currentGroups.size() - 1);
                markUsed(group, hand, used, false);
            }
        }
    }

    private List<CardGroup> generateAllPossibleGroups(List<Card> hand) {
        List<CardGroup> groups = new ArrayList<>();
        int n = hand.size();

        // This is a simplified group generation. In a full version, we would
        // iterate through all combinations of 3, 4, 5... cards.
        // Since it's only 13 cards, we can use combinations.

        // 3-card combinations
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                for (int k = j + 1; k < n; k++) {
                    List<Card> combo = Arrays.asList(hand.get(i), hand.get(j), hand.get(k));
                    CardGroup cg = RummyRules.evaluateGroup(combo, jokerConfig);
                    if (cg != null) groups.add(cg);
                }
            }
        }

        // 4-card combinations
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                for (int k = j + 1; k < n; k++) {
                    for (int l = k + 1; l < n; l++) {
                        List<Card> combo = Arrays.asList(hand.get(i), hand.get(j), hand.get(k), hand.get(l));
                        CardGroup cg = RummyRules.evaluateGroup(combo, jokerConfig);
                        if (cg != null) groups.add(cg);
                    }
                }
            }
        }

        return groups;
    }

    private boolean canAddGroup(CardGroup group, List<Card> hand, boolean[] used) {
        for (Card c : group.getCards()) {
            int idx = hand.indexOf(c);
            if (idx == -1 || used[idx]) return false;
        }
        return true;
    }

    private void markUsed(CardGroup group, List<Card> hand, boolean[] used, boolean value) {
        for (Card c : group.getCards()) {
            used[hand.indexOf(c)] = value;
        }
    }

    private int calculatePoints(List<Card> deadwood) {
        int sum = 0;
        for (Card c : deadwood) {
            sum += RummyRules.getCardPoints(c);
        }
        return sum;
    }

    private boolean checkValidity(List<CardGroup> groups) {
        if (groups.size() < 2) return false;

        boolean hasPure = false;
        int sequenceCount = 0;

        for (CardGroup g : groups) {
            if (g.getType() == CardGroup.GroupType.PURE_SEQUENCE) {
                hasPure = true;
                sequenceCount++;
            } else if (g.getType() == CardGroup.GroupType.IMPURE_SEQUENCE) {
                sequenceCount++;
            }
        }

        return hasPure && sequenceCount >= 2;
    }
}
