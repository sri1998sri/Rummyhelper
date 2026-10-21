package com.rummy.assistant.engine;

import com.rummy.assistant.model.Card;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CardSorter {

    public enum SortMode {
        SIMPLE,
        GROUP_AWARE
    }

    public static List<Card> sort(List<Card> hand, SortMode mode) {
        if (hand == null || hand.isEmpty()) return new ArrayList<>();
        List<Card> sortedHand = new ArrayList<>(hand);

        if (mode == SortMode.SIMPLE) {
            Collections.sort(sortedHand, new SimpleCardComparator());
        } else if (mode == SortMode.GROUP_AWARE) {
            // For group-aware sorting, we use the solver to find the best arrangement
            // and sort based on those groups.
            sortedHand = sortGroupAware(sortedHand);
        }

        return sortedHand;
    }

    private static List<Card> sortGroupAware(List<Card> hand) {
        // We use a dummy JokerConfig for sorting purposes
        com.rummy.assistant.model.JokerConfig config = new com.rummy.assistant.model.JokerConfig(com.rummy.assistant.model.Rank.TWO);
        RummySolver solver = new RummySolver(config);
        HandArrangement arrangement = solver.solve(hand);

        if (arrangement == null) {
            // Fallback to simple sort
            Collections.sort(hand, new SimpleCardComparator());
            return hand;
        }

        List<Card> result = new ArrayList<>();
        // Add cards from groups first
        for (CardGroup group : arrangement.getGroups()) {
            result.addAll(group.getCards());
        }
        // Add deadwood at the end
        result.addAll(arrangement.getDeadwood());

        return result;
    }

    private static class SimpleCardComparator implements Comparator<Card> {
        @Override
        public int compare(Card c1, Card c2) {
            if (c1.isJoker() && !c2.isJoker()) return -1;
            if (!c1.isJoker() && c2.isJoker()) return 1;
            if (c1.isJoker() && c2.isJoker()) return 0;

            if (c1.getSuit() != c2.getSuit()) {
                return c1.getSuit().compareTo(c2.getSuit());
            }
            return Integer.compare(c1.getRank().getValue(), c2.getRank().getValue());
        }
    }
}
