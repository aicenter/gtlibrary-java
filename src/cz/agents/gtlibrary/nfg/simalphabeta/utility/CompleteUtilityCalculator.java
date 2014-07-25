/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.nfg.simalphabeta.utility;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.Killer;
import cz.agents.gtlibrary.nfg.simalphabeta.Result;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.DoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;

public class CompleteUtilityCalculator implements UtilityCalculator {

    private Data data;
    private DOCache cache;

    public CompleteUtilityCalculator(Data data) {
        this.data = data;
        this.cache = data.getCache();
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

        if(Killer.kill)
            return Double.NaN;
        if (p1Bound - p2Bound < 1e-8) {
            cache.setStrategy(s1, s2, natureStrategy, getStrategies(p1Bound, p2Bound));
            Stats.getInstance().incrementABCuts();
            return p1Bound;
        }
        DoubleOracle oracle = data.getDoubleOracle(state, 0, 0);

        oracle.generate();
        cache.setStrategy(s1, s2, natureStrategy, new Result[]{new Result(oracle.getGameValue(), oracle.getStrategyFor(state.getAllPlayers()[0])),
                new Result(-oracle.getGameValue(), oracle.getStrategyFor(state.getAllPlayers()[1]))});
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

    public Result[] getStrategies(double p1Bound, double p2Bound) {
        Action p1Action = data.getAlphaBetaFor(data.gameInfo.getAllPlayers()[1]).getTopLevelAction(data.gameInfo.getAllPlayers()[0]);
        Action p2Action = data.getAlphaBetaFor(data.gameInfo.getAllPlayers()[0]).getTopLevelAction(data.gameInfo.getAllPlayers()[1]);
        MixedStrategy p1Mixed = new MixedStrategy();
        MixedStrategy p2Mixed = new MixedStrategy();

        p1Mixed.put(new ActionPureStrategy(p1Action), 1d);
        p2Mixed.put(new ActionPureStrategy(p2Action), 1d);
        return new Result[]{new Result(p1Bound, p1Mixed), new Result(p2Bound, p2Mixed)};
    }
}
