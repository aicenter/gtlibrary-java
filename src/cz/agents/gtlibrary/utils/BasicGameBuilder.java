package cz.agents.gtlibrary.utils;

import cz.agents.gtlibrary.interfaces.*;

import java.util.ArrayDeque;

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
            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }
        System.out.println("State count: " + stateCounter);
    }
}
