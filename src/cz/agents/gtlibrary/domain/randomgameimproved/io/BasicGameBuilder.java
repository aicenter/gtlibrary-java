package cz.agents.gtlibrary.domain.randomgameimproved.io;

import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.interfaces.*;

import java.util.ArrayDeque;
import java.util.stream.Collectors;

public class BasicGameBuilder {
    public static void main(String[] args) {
        GameState root = new GenericPokerGameState();
        Expander<MCTSInformationSet> expander = new GenericPokerExpander<>(new MCTSConfig());
        new GPGameInfo();

        build(root, expander.getAlgorithmConfig(), expander);
        System.out.println(expander.getAlgorithmConfig().getAllInformationSets().values().stream().filter(i -> i.getAllStates().stream().allMatch(s -> !s.isGameEnd())).count());
    }

    public static void build(GameState rootState, AlgorithmConfig<? extends InformationSet> algConfig, Expander<? extends InformationSet> expander) {
        ArrayDeque<GameState> queue = new ArrayDeque<>();

        queue.add(rootState);

        while (queue.size() > 0) {
            GameState currentState = queue.removeLast();

            algConfig.addInformationSetFor(currentState);
            if (currentState.isGameEnd())
                continue;
            queue.addAll(expander.getActions(currentState).stream().map(currentState::performAction).collect(Collectors.toList()));

            if(algConfig.getAllInformationSets().size() % 100000 == 0)
                System.out.println(algConfig.getAllInformationSets().size());
        }
    }
}
