package com.rummy.assistant.engine;

import com.rummy.assistant.model.Card;
import com.rummy.assistant.model.JokerConfig;
import com.rummy.assistant.model.Rank;
import com.rummy.assistant.model.Suit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RummyRules {

    public static int getCardPoints(Card card) {
        if (card.isJoker()) return 0;
        Rank rank = card.getRank();
        if (rank == Rank.ACE) return 10;
        if (rank == Rank.JACK || rank == Rank.QUEEN || rank == Rank.KING) return 10;
        return rank.getValue();
    }

    public static CardGroup evaluateGroup(List<Card> cards, JokerConfig jokerConfig) {
        if (cards.size() < 3) return null;

        // 1. Check for Set (Same rank, different suits)
        if (isSet(cards, jokerConfig)) {
            return new CardGroup(cards, CardGroup.GroupType.SET);
        }

        // 2. Check for Sequence
        if (isSequence(cards, jokerConfig)) {
            boolean isPure = isPureSequence(cards, jokerConfig);
            return new CardGroup(cards, isPure ? CardGroup.GroupType.PURE_SEQUENCE : CardGroup.GroupType.IMPURE_SEQUENCE);
        }

        return null;
    }

    private static boolean isSet(List<Card> cards, JokerConfig jokerConfig) {
        // A set consists of 3 or 4 cards of the same rank but different suits.
        // Jokers can substitute for any card.

        // Find the non-joker rank. If all are jokers, it's a set of any rank (usually handled as sequence).
        Rank targetRank = null;
        int jokerCount = 0;
        List<Suit> suits = new ArrayList<>();

        for (Card c : cards) {
            if (jokerConfig.isAnyJoker(c)) {
                jokerCount++;
            } else {
                if (targetRank == null) {
                    targetRank = c.getRank();
                } else if (c.getRank() != targetRank) {
                    return false; // Mixed ranks among non-jokers
                }
                if (suits.contains(c.getSuit())) return false; // Duplicate suit
                suits.add(c.getSuit());
            }
        }

        return true; // Either all jokers or all non-jokers have same rank and different suits
    }

    private static boolean isSequence(List<Card> cards, JokerConfig jokerConfig) {
        // Cards must be of same suit (unless joker)
        Suit suit = null;
        List<Integer> ranks = new ArrayList<>();
        int jokers = 0;

        for (Card c : cards) {
            if (jokerConfig.isAnyJoker(c)) {
                jokers++;
            } else {
                if (suit == null) suit = c.getSuit();
                else if (c.getSuit() != suit) return false;
                ranks.add(c.getRank().getValue());
            }
        }

        if (ranks.isEmpty()) return true; // All jokers

        Collections.sort(ranks);

        // Check for duplicates among non-jokers
        for (int i = 0; i < ranks.size() - 1; i++) {
            if (ranks.get(i).equals(ranks.get(i+1))) return false;
        }

        // Check if the range fits in a sequence of size 'cards.size()'
        int minRank = ranks.get(0);
        int maxRank = ranks.get(ranks.size() - 1);

        return (maxRank - minRank + 1) <= cards.size();
    }

    private static boolean isPureSequence(List<Card> cards, JokerConfig jokerConfig) {
        for (Card c : cards) {
            if (jokerConfig.isAnyJoker(c)) return false;
        }
        return isSequence(cards, jokerConfig);
    }
}
