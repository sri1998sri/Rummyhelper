package com.rummy.assistant.engine;

import com.rummy.assistant.model.*;
import java.util.*;

public class RummySolverTest {

    public static void main(String[] args) {
        System.out.println("Running RummySolver Tests...");

        testPureSequence();
        testImpureSequence();
        testValidSet();
        testWinningHand();
        testInvalidHandNoPure();

        System.out.println("Tests completed.");
    }

    private static void testPureSequence() {
        JokerConfig config = new JokerConfig(Rank.TWO);
        RummySolver solver = new RummySolver(config);
        List<Card> hand = new ArrayList<>();
        hand.add(new Card(Rank.THREE, Suit.SPADES));
        hand.add(new Card(Rank.FOUR, Suit.SPADES));
        hand.add(new Card(Rank.FIVE, Suit.SPADES));
        // Fill others as deadwood
        for (int i = 0; i < 10; i++) hand.add(new Card(Rank.TEN, Suit.HEARTS));

        HandArrangement res = solver.solve(hand);
        System.out.println("Pure Sequence Test: " + (res != null && res.getGroups().stream().anyMatch(g -> g.getType() == CardGroup.GroupType.PURE_SEQUENCE) ? "PASS" : "FAIL"));
    }

    private static void testImpureSequence() {
        JokerConfig config = new JokerConfig(Rank.TWO); // 2 is wild joker
        RummySolver solver = new RummySolver(config);
        List<Card> hand = new ArrayList<>();
        hand.add(new Card(Rank.THREE, Suit.SPADES));
        hand.add(new Card(Rank.TWO, Suit.HEARTS)); // Wild Joker
        hand.add(new Card(Rank.FIVE, Suit.SPADES));
        for (int i = 0; i < 10; i++) hand.add(new Card(Rank.TEN, Suit.HEARTS));

        HandArrangement res = solver.solve(hand);
        System.out.println("Impure Sequence Test: " + (res != null && res.getGroups().stream().anyMatch(g -> g.getType() == CardGroup.GroupType.IMPURE_SEQUENCE) ? "PASS" : "FAIL"));
    }

    private static void testValidSet() {
        JokerConfig config = new JokerConfig(Rank.TWO);
        RummySolver solver = new RummySolver(config);
        List<Card> hand = new ArrayList<>();
        hand.add(new Card(Rank.KING, Suit.SPADES));
        hand.add(new Card(Rank.KING, Suit.HEARTS));
        hand.add(new Card(Rank.KING, Suit.DIAMONDS));
        for (int i = 0; i < 10; i++) hand.add(new Card(Rank.TEN, Suit.CLUBS));

        HandArrangement res = solver.solve(hand);
        System.out.println("Valid Set Test: " + (res != null && res.getGroups().stream().anyMatch(g -> g.getType() == CardGroup.GroupType.SET) ? "PASS" : "FAIL"));
    }

    private static void testWinningHand() {
        JokerConfig config = new JokerConfig(Rank.TWO);
        RummySolver solver = new RummySolver(config);
        List<Card> hand = new ArrayList<>();
        // Pure Sequence
        hand.add(new Card(Rank.THREE, Suit.SPADES));
        hand.add(new Card(Rank.FOUR, Suit.SPADES));
        hand.add(new Card(Rank.FIVE, Suit.SPADES));
        // Impure Sequence
        hand.add(new Card(Rank.SIX, Suit.HEARTS));
        hand.add(new Card(Rank.TWO, Suit.CLUBS)); // Wild Joker
        hand.add(new Card(Rank.EIGHT, Suit.HEARTS));
        // Set
        hand.add(new Card(Rank.JACK, Suit.SPADES));
        hand.add(new Card(Rank.JACK, Suit.HEARTS));
        hand.add(new Card(Rank.JACK, Suit.DIAMONDS));
        // Others
        for (int i = 0; i < 4; i++) hand.add(new Card(Rank.TWO, Suit.CLUBS));

        HandArrangement res = solver.solve(hand);
        System.out.println("Winning Hand Test: " + (res != null && res.isValid() ? "PASS" : "FAIL"));
    }

    private static void testInvalidHandNoPure() {
        JokerConfig config = new JokerConfig(Rank.TWO);
        RummySolver solver = new RummySolver(config);
        List<Card> hand = new ArrayList<>();
        // Impure Sequence 1
        hand.add(new Card(Rank.THREE, Suit.SPADES));
        hand.add(new Card(Rank.TWO, Suit.HEARTS));
        hand.add(new Card(Rank.FIVE, Suit.SPADES));
        // Impure Sequence 2
        hand.add(new Card(Rank.SIX, Suit.HEARTS));
        hand.add(new Card(Rank.TWO, Suit.CLUBS));
        hand.add(new Card(Rank.EIGHT, Suit.HEARTS));
        // Set
        hand.add(new Card(Rank.JACK, Suit.SPADES));
        hand.add(new Card(Rank.JACK, Suit.HEARTS));
        hand.add(new Card(Rank.JACK, Suit.DIAMONDS));
        for (int i = 0; i < 4; i++) hand.add(new Card(Rank.TEN, Suit.CLUBS));

        HandArrangement res = solver.solve(hand);
        System.out.println("Invalid Hand (No Pure) Test: " + (res != null && !res.isValid() ? "PASS" : "FAIL"));
    }
}
