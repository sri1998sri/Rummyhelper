package com.rummy.assistant.engine;

import com.rummy.assistant.model.Card;
import java.util.List;
import java.util.Objects;

public class CardGroup {
    public enum GroupType {
        PURE_SEQUENCE,
        IMPURE_SEQUENCE,
        SET
    }

    private final List<Card> cards;
    private final GroupType type;

    public CardGroup(List<Card> cards, GroupType type) {
        this.cards = cards;
        this.type = type;
    }

    public List<Card> getCards() { return cards; }
    public GroupType getType() { return type; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(type).append(": ");
        for (int i = 0; i < cards.size(); i++) {
            sb.append(cards.get(i).toString());
            if (i < cards.size() - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardGroup that = (CardGroup) o;
        return Objects.equals(cards, that.cards) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cards, type);
    }
}
