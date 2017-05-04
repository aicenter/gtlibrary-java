package cz.agents.gtlibrary.domain.randomgameimproved.io;

import cz.agents.gtlibrary.interfaces.*;

import java.util.ArrayDeque;
import java.util.stream.Collectors;

public class BasicGameBuilder {
    public static void build(GameState rootState, AlgorithmConfig<? extends InformationSet> algConfig, Expander<? extends InformationSet> expander) {
        ArrayDeque<GameState> queue = new ArrayDeque<>();

        queue.add(rootState);

        while (queue.size() > 0) {
            GameState currentState = queue.removeLast();

            algConfig.addInformationSetFor(currentState);
            if (currentState.isGameEnd())
                continue;
            queue.addAll(expander.getActions(currentState).stream().map(currentState::performAction).collect(Collectors.toList()));
        }
    }
}
