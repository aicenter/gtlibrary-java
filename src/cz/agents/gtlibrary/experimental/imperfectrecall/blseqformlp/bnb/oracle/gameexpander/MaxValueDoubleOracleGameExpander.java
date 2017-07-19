package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.gameexpander;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.DoubleOracleIRConfig;
import cz.agents.gtlibrary.interfaces.*;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;

public class MaxValueDoubleOracleGameExpander extends TempLeafDoubleOracleGameExpander {
    Map<GameState, Double> p1HighestReachableUtility = new HashMap<>();

    public MaxValueDoubleOracleGameExpander(Player maxPlayer, GameState root, Expander<SequenceFormIRInformationSet> expander, GameInfo info) {
        super(maxPlayer, root, expander, info);
    }

//    @Override
//    protected double getUtilityUBForCombo(GameState state, Map<Sequence, Set<Action>> minPlayerBestResponses, DoubleOracleIRConfig config) {
//        return p1HighestReachableUtility.computeIfAbsent(state, s -> getHighestReachableUtility(state));
//    }
//
//    private double getHighestReachableUtility(GameState state) {
//        if (state.isGameEnd())
//            return state.getUtilities()[0];
//        Double reward = p1HighestReachableUtility.get(state);
//
//        if (reward != null)
//            return reward;
//        Map<Action, GameState> successors = getStateCache().computeIfAbsent(state, s -> new HashMap<>());
//        OptionalDouble max = expander.getActions(state).stream().map(a -> successors.computeIfAbsent(a, action -> state.performAction(a))).mapToDouble(s -> getHighestReachableUtility(s)).max();
//
//        p1HighestReachableUtility.put(state, max.getAsDouble());
//        return max.getAsDouble();
//    }
}
