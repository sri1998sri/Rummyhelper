package com.rummy.assistant.model;

public class JokerConfig {
    private Rank wildJokerRank;

    public JokerConfig(Rank wildJokerRank) {
        this.wildJokerRank = wildJokerRank;
    }

    public Rank getWildJokerRank() {
        return wildJokerRank;
    }

    public void setWildJokerRank(Rank wildJokerRank) {
        this.wildJokerRank = wildJokerRank;
    }

    public boolean isWildJoker(Card card) {
        if (card.isJoker()) return card.isWildJoker();
        return card.getRank() == wildJokerRank;
    }

    public boolean isPrintedJoker(Card card) {
        return card.isJoker() && !card.isWildJoker();
    }

    public boolean isAnyJoker(Card card) {
        return isWildJoker(card) || isPrintedJoker(card);
    }
}
