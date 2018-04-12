package cz.agents.gtlibrary.utils;

import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.ArrayDeque;
import java.util.stream.Collectors;

public class BasicGameBuilder {
    public static void build(GameState rootState, AlgorithmConfig<? extends InformationSet> algConfig, Expander<? extends InformationSet> expander) {
        ArrayDeque<GameState> queue = new ArrayDeque<>();
        int stateCounter = 0;

        queue.add(rootState);

        while (queue.size() > 0) {
            GameState currentState = queue.removeLast();

            stateCounter++;
            algConfig.addInformationSetFor(currentState);
            if (currentState.isGameEnd())
                continue;
            queue.addAll(expander.getActions(currentState).stream().map(currentState::performAction).collect(Collectors.toList()));
            if (stateCounter % 100000 == 0) {
                System.out.println("states: " + stateCounter);
                System.out.println("iss: " + algConfig.getAllInformationSets().size());
            }
        }
        System.out.println("State count: " + stateCounter);
    }
}
