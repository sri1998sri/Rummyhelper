package com.rummy.assistant.engine;

import com.rummy.assistant.model.Card;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HandArrangement {
    private final List<CardGroup> groups;
    private final List<Card> deadwood;
    private final int totalPoints;
    private final boolean isValid;

    public HandArrangement(List<CardGroup> groups, List<Card> deadwood, int totalPoints, boolean isValid) {
        this.groups = groups;
        this.deadwood = deadwood;
        this.totalPoints = totalPoints;
        this.isValid = isValid;
    }

    public List<CardGroup> getGroups() { return groups; }
    public List<Card> getDeadwood() { return deadwood; }
    public int getTotalPoints() { return totalPoints; }
    public boolean isValid() { return isValid; }

    @Override
    public String toString() {
        return "HandArrangement{" +
                "groups=" + groups +
                ", deadwood=" + deadwood +
                ", points=" + totalPoints +
                ", valid=" + isValid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HandArrangement that = (HandArrangement) o;
        return totalPoints == that.totalPoints && isValid == that.isValid &&
               Objects.equals(groups, that.groups) && Objects.equals(deadwood, that.deadwood);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groups, deadwood, totalPoints, isValid);
    }
}
