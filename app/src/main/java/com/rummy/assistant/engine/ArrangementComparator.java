package com.rummy.assistant.engine;

import com.rummy.assistant.model.Card;
import com.rummy.assistant.model.JokerConfig;
import java.util.ArrayList;
import java.util.List;

public class ArrangementComparator {

    public enum Scenario {
        REORGANIZE,
        SIMULATE_DISCARD,
        SIMULATE_REPLACE
    }

    public static class ComparisonResult {
        public final HandArrangement current;
        public final HandArrangement alternative;
        public final String scenarioDescription;

        public ComparisonResult(HandArrangement current, HandArrangement alternative, String description) {
            this.current = current;
            this.alternative = alternative;
            this.scenarioDescription = description;
        }
    }

    public ComparisonResult compare(List<Card> originalHand, Card selectedCard, Card replacementCard, Scenario scenario, JokerConfig jokerConfig) {
        RummySolver solver = new RummySolver(jokerConfig);

        // 1. Calculate Current Best
        HandArrangement currentBest = solver.solve(originalHand);

        // 2. Calculate Alternative based on scenario
        List<Card> modifiedHand = new ArrayList<>(originalHand);
        String description = "";

        switch (scenario) {
            case REORGANIZE:
                // In re-organize, we just solve again (could be used if we have multiple solutions)
                description = "Reorganizing current hand";
                break;
            case SIMULATE_DISCARD:
                modifiedHand.remove(selectedCard);
                description = "Simulating discard of " + selectedCard;
                break;
            case SIMULATE_REPLACE:
                modifiedHand.remove(selectedCard);
                modifiedHand.add(replacementCard);
                description = "Replacing " + selectedCard + " with " + replacementCard;
                break;
        }

        HandArrangement alternativeBest = solver.solve(modifiedHand);

        return new ComparisonResult(currentBest, alternativeBest, description);
    }
}
