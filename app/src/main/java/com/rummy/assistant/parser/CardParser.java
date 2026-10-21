package com.rummy.assistant.parser;

import com.rummy.assistant.model.Card;
import com.rummy.assistant.model.Rank;
import com.rummy.assistant.model.Suit;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardParser {

    // Patterns for different common card label formats
    private static final Pattern PATTERN_OF = Pattern.compile("(\\d+|[AJQK])\\s+of\\s+(Spades|Hearts|Diamonds|Clubs)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_SYMBOL = Pattern.compile("(\\d+|[AJQK])([♠♥♦♣])");
    private static final Pattern PATTERN_WORD_RANK = Pattern.compile("(Ace|Two|Three|Four|Five|Six|Seven|Eight|Nine|Ten|Jack|Queen|King)\\s+of\\s+(Spades|Hearts|Diamonds|Clubs)", Pattern.CASE_INSENSITIVE);

    public static Card parse(String text) {
        if (text == null || text.isEmpty()) return null;
        text = text.trim();

        // Try "X of Suit" (e.g., "7 of Spades")
        Matcher ofMatcher = PATTERN_OF.matcher(text);
        if (ofMatcher.find()) {
            return createCard(ofMatcher.group(1), ofMatcher.group(2));
        }

        // Try "X Symbol" (e.g., "7♠")
        Matcher symbolMatcher = PATTERN_SYMBOL.matcher(text);
        if (symbolMatcher.find()) {
            return createCard(symbolMatcher.group(1), symbolMatcher.group(2));
        }

        // Try "Worded rank of Suit" (e.g., "Seven of Spades")
        Matcher wordMatcher = PATTERN_WORD_RANK.matcher(text);
        if (wordMatcher.find()) {
            return createCard(wordMatcher.group(1), wordMatcher.group(2));
        }

        return null;
    }

    private static Card createCard(String rankStr, String suitStr) {
        Rank rank = parseRank(rankStr);
        Suit suit = parseSuit(suitStr);
        if (rank != null && suit != null) {
            return new Card(rank, suit);
        }
        return null;
    }

    private static Rank parseRank(String rankStr) {
        rankStr = rankStr.toUpperCase();
        switch (rankStr) {
            case "A": case "ACE": return Rank.ACE;
            case "2": case "TWO": return Rank.TWO;
            case "3": case "THREE": return Rank.THREE;
            case "4": case "FOUR": return Rank.FOUR;
            case "5": case "FIVE": return Rank.FIVE;
            case "6": case "SIX": return Rank.SIX;
            case "7": case "SEVEN": return Rank.SEVEN;
            case "8": case "EIGHT": return Rank.EIGHT;
            case "9": case "NINE": return Rank.NINE;
            case "10": case "TEN": return Rank.TEN;
            case "J": case "JACK": return Rank.JACK;
            case "Q": case "QUEEN": return Rank.QUEEN;
            case "K": case "KING": return Rank.KING;
            default: return null;
        }
    }

    private static Suit parseSuit(String suitStr) {
        suitStr = suitStr.trim();
        if (suitStr.equalsIgnoreCase("Spades") || suitStr.equals("♠")) return Suit.SPADES;
        if (suitStr.equalsIgnoreCase("Hearts") || suitStr.equals("♥")) return Suit.HEARTS;
        if (suitStr.equalsIgnoreCase("Diamonds") || suitStr.equals("♦")) return Suit.DIAMONDS;
        if (suitStr.equalsIgnoreCase("Clubs") || suitStr.equals("♣")) return Suit.CLUBS;
        return null;
    }

    public static List<Card> parseMultiple(String text) {
        List<Card> cards = new ArrayList<>();
        // This is a simple implementation that looks for any card-like patterns in a long string
        // In a real app, we might split by commas or whitespace
        String[] parts = text.split("[,\\s]+");
        for (String part : parts) {
            Card card = parse(part);
            if (card != null) cards.add(card);
        }
        return cards;
    }
}
