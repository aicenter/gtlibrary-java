package cz.agents.gtlibrary.domain.randomgameimproved.io;

import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.IIGoofSpielGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.pursuit.PursuitExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameInfo;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameState;
import cz.agents.gtlibrary.domain.pursuit.VisibilityPursuitGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.interfaces.*;

import java.util.ArrayDeque;
import java.util.stream.Collectors;

public class BasicGameBuilder {
    public static void main(String[] args) {
//        buildVisibilityPursuit();
//        buildGP();
        buildGS();
//        buildRandomGame();
    }

    private static void buildVisibilityPursuit() {
        GameState root = new VisibilityPursuitGameState();
        Expander<MCTSInformationSet> expander = new PursuitExpander<>(new MCTSConfig());
        new PursuitGameInfo();

        buildWithoutTerminalIS(root, expander.getAlgorithmConfig(), expander);
        System.out.println(expander.getAlgorithmConfig().getAllInformationSets().values().stream().filter(i -> i.getPlayer().getId() != 2).filter(i -> i.getAllStates().stream().allMatch(s -> !s.isGameEnd())).count());
    }

    private static void buildRandomGame() {
        GameState root = new RandomGameState();
        Expander<MCTSInformationSet> expander = new RandomGameExpander<>(new MCTSConfig());
        new RandomGameInfo();

        buildWithoutTerminalIS(root, expander.getAlgorithmConfig(), expander);
        System.out.println(expander.getAlgorithmConfig().getAllInformationSets().values().stream().filter(i -> i.getPlayer().getId() != 2).filter(i -> i.getAllStates().stream().allMatch(s -> !s.isGameEnd())).count());
    }

    private static void buildGP() {
        GameState root = new GenericPokerGameState();
        Expander<MCTSInformationSet> expander = new GenericPokerExpander<>(new MCTSConfig());
        new GPGameInfo();

        buildWithoutTerminalIS(root, expander.getAlgorithmConfig(), expander);
        System.out.println(expander.getAlgorithmConfig().getAllInformationSets().values().stream().filter(i -> i.getPlayer().getId() != 2).filter(i -> i.getAllStates().stream().allMatch(s -> !s.isGameEnd())).count());
    }

    private static void buildGS() {
        GameState root = new IIGoofSpielGameState();
        Expander<MCTSInformationSet> expander = new GoofSpielExpander<>(new MCTSConfig());
        new GSGameInfo();

        buildWithoutTerminalIS(root, expander.getAlgorithmConfig(), expander);
        System.out.println(expander.getAlgorithmConfig().getAllInformationSets().values().stream().filter(i -> i.getPlayer().getId() != 2).filter(i -> i.getAllStates().stream().allMatch(s -> !s.isGameEnd())).count());
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

    public static void buildWithoutTerminalIS(GameState rootState, AlgorithmConfig<? extends InformationSet> algConfig, Expander<? extends InformationSet> expander) {
        ArrayDeque<GameState> queue = new ArrayDeque<>();

        queue.add(rootState);

        while (queue.size() > 0) {
            GameState currentState = queue.removeLast();

            if (currentState.isGameEnd())
                continue;
            algConfig.addInformationSetFor(currentState);
            queue.addAll(expander.getActions(currentState).stream().map(currentState::performAction).collect(Collectors.toList()));

            if(algConfig.getAllInformationSets().size() % 1000 == 0)
                System.out.println(expander.getAlgorithmConfig().getAllInformationSets().values().stream().filter(i -> i.getPlayer().getId() != 2).filter(i -> i.getAllStates().stream().allMatch(s -> !s.isGameEnd())).count());
        }
    }
}
