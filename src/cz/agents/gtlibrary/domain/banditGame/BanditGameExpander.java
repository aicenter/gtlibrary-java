package cz.agents.gtlibrary.domain.banditGame;

import cz.agents.gtlibrary.domain.observationGame.ObsGameFollowerAction;
import cz.agents.gtlibrary.domain.observationGame.ObsGameInfo;
import cz.agents.gtlibrary.domain.observationGame.ObsGameState;
import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by kail on 11/11/17.
 */
public class BanditGameExpander<I extends InformationSet> extends ExpanderImpl<I> {
    public BanditGameExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        if (gameState.getPlayerToMove().equals(BanditGameInfo.AGENT))
            return getAgentActions((BanditGameState) gameState);
        return getBanditActions((BanditGameState) gameState);
    }

    private List<Action> getAgentActions(BanditGameState state) {
        List<Action> actions = new ArrayList<Action>();

        if (state.timeStep == 0) {
            actions.add(new BanditGameAgentAction(getAlgorithmConfig().getInformationSetFor(state), -1, -1, BanditGameInfo.START.getLeft(), BanditGameInfo.START.getRight()));
        } else {
            for (int i = -1; i <= 1; i++)
                for (int j = -1; j <= 1; j++) {
                    if (i*j != 0) continue;
                    Pair<Integer, Integer> a = new Pair<>(state.agentRow + i, state.agentCol + j);
                    if (!state.history.contains(a) && BanditGameInfo.map[a.getLeft()][a.getRight()] != '#')
                        actions.add(new BanditGameAgentAction(getAlgorithmConfig().getInformationSetFor(state), state.agentRow, state.agentCol, a.getLeft(), a.getRight()));
                }
        }
        return actions;

    }


    private List<Action> getBanditActions(BanditGameState state) {
        List<Action> actions = new ArrayList<Action>();

        if (state.timeStep == 0 ) {
            assert state.bandits.size() < BanditGameInfo.BANDIT_NUM;
            for (int i=0; i<BanditGameInfo.DGRS.size(); i++) {
                Pair<Integer, Integer> a = BanditGameInfo.DGRS.get(i);
                if (!state.bandits.contains(a))
                    actions.add(new BanditGameBanditAction(getAlgorithmConfig().getInformationSetFor(state), a.getLeft(), a.getRight()));
            }
        } else {
            List<Pair<Integer,Integer>> freeSpots = BanditGameInfo.DGRS.stream().filter(p -> !state.bandits.contains(p)).collect(Collectors.toList());
            for (Pair<Integer,Integer> p : state.bandits) {
                actions.add(new BanditGameBanditAction(getAlgorithmConfig().getInformationSetFor(state), p.getLeft(), p.getRight(), p.getLeft(), p.getRight()));
                for (Pair<Integer,Integer> s : freeSpots) {
                    if (s.getLeft() == state.agentRow && s.getRight() == state.agentCol) continue;
                    actions.add(new BanditGameBanditAction(getAlgorithmConfig().getInformationSetFor(state), p.getLeft(), p.getRight(), s.getLeft(), s.getRight()));
                }
            }
        }

        if (actions.size() == 0)
           actions.add(new BanditGameBanditAction(getAlgorithmConfig().getInformationSetFor(state)));
        return actions;
    }
}
