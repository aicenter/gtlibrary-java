package cz.agents.gtlibrary.nfg.simalphabeta.utility;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.DoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;

public class CompleteUtilityCalculator implements UtilityCalculator {

    private Data data;

    public CompleteUtilityCalculator(Data data) {
        this.data = data;
    }

    public double getUtilities(GameState state, ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta) {
        throw new UnsupportedOperationException();
    }

    protected double computeUtilityForNature(GameState state, ActionPureStrategy s1, ActionPureStrategy s2) {
        double utilityValue = 0;

        for (Action action : data.expander.getActions(state)) {
            utilityValue += state.getProbabilityOfNatureFor(action) * computeUtilityOf(state.performAction(action), s1, s2);
        }
        return utilityValue;
    }

    protected double computeUtilityOf(GameState state, ActionPureStrategy s1, ActionPureStrategy s2) {
        double p1Bound = data.getAlphaBetaFor(state.getAllPlayers()[0]).getUnboundedValue(state);
        double p2Bound = -data.getAlphaBetaFor(state.getAllPlayers()[1]).getUnboundedValue(state);
        ActionPureStrategy natureStrategy = getNatureStrategy(state);

        if (p1Bound - p2Bound < 1e-8) {
            data.getCache().setStrategy(s1, s2, natureStrategy, getStrategies());
            Stats.getInstance().incrementABCuts();
            return p1Bound;
        }
        DoubleOracle oracle = data.getDoubleOracle(state, 0, 0);

        oracle.generate();
        data.getCache().setStrategy(s1, s2, natureStrategy, new MixedStrategy[]{oracle.getStrategyFor(state.getAllPlayers()[0]),
                oracle.getStrategyFor(state.getAllPlayers()[1])});
        return oracle.getGameValue();
    }

    private ActionPureStrategy getNatureStrategy(GameState state) {
        if (state.getAllPlayers().length < 3)
            return null;
        Sequence natureSequence = state.getSequenceFor(state.getAllPlayers()[2]);

        if (natureSequence.size() == 0)
            return null;
        return new ActionPureStrategy(natureSequence.getLast());
    }

    public double getUtility(GameState state, ActionPureStrategy s1, ActionPureStrategy s2) {
        if (state.isPlayerToMoveNature())
            return computeUtilityForNature(state, s1, s2);
        return computeUtilityOf(state, s1, s2);
    }

    @Override
    public double getUtilitiesForIncreasedBounds(GameState state, ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta) {
        throw new UnsupportedOperationException();
    }

    public MixedStrategy<ActionPureStrategy>[] getStrategies() {
        Action p1Action = data.getAlphaBetaFor(data.gameInfo.getAllPlayers()[0]).getTopLevelAction(data.gameInfo.getAllPlayers()[0]);
        Action p2Action = data.getAlphaBetaFor(data.gameInfo.getAllPlayers()[0]).getTopLevelAction(data.gameInfo.getAllPlayers()[1]);
        MixedStrategy<ActionPureStrategy>[] strategies = new MixedStrategy[]{new MixedStrategy(), new MixedStrategy()};

        strategies[0].put(new ActionPureStrategy(p1Action), 1d);
        strategies[1].put(new ActionPureStrategy(p2Action), 1d);
        return strategies;
    }
}
