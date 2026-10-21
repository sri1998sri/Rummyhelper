package com.rummy.assistant.model;

import java.util.Objects;

public class Card {
    private final Rank rank;
    private final Suit suit;
    private final boolean isJoker;
    private final boolean isWildJoker;

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
        this.isJoker = false;
        this.isWildJoker = false;
    }

    public Card(boolean isJoker, boolean isWildJoker) {
        this.rank = null;
        this.suit = null;
        this.isJoker = isJoker;
        this.isWildJoker = isWildJoker;
    }

    public Rank getRank() { return rank; }
    public Suit getSuit() { return suit; }
    public boolean isJoker() { return isJoker; }
    public boolean isWildJoker() { return isWildJoker; }

    @Override
    public String toString() {
        if (isJoker) {
            return isWildJoker ? "Wild Joker" : "Printed Joker";
        }
        return rank.getSymbol() + suit.getSymbol();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return isJoker == card.isJoker &&
               isWildJoker == card.isWildJoker &&
               rank == card.rank &&
               suit == card.suit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank, suit, isJoker, isWildJoker);
    }
}
