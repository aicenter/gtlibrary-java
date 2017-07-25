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


package cz.agents.gtlibrary.nfg.simalphabeta.alphabeta;

import cz.agents.gtlibrary.iinodes.SimultaneousGameState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Killer;
import cz.agents.gtlibrary.nfg.simalphabeta.Result;
import cz.agents.gtlibrary.nfg.simalphabeta.SimABInformationSet;
import cz.agents.gtlibrary.nfg.simalphabeta.SimAlphaBeta;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.AlphaBetaCache;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.NullDOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;
import cz.agents.gtlibrary.utils.Triplet;

import java.util.List;
import java.util.ListIterator;

public abstract class AlphaBetaImpl implements AlphaBeta {

    protected Player player;
    protected AlphaBetaCache cache;
    protected Expander<SimABInformationSet> expander;
    protected AlgorithmConfig<SimABInformationSet> algConfig;
    protected GameInfo gameInfo;
    private Action p1Action;
    private Action p2Action;
    private Action tempAction;

    public AlphaBetaImpl(Player player, Expander<SimABInformationSet> expander, AlphaBetaCache cache, GameInfo gameInfo) {
        this.player = player;
        this.expander = expander;
        this.cache = cache;
        this.algConfig = expander.getAlgorithmConfig();
        this.gameInfo = gameInfo;
    }

    public double getUnboundedValue(GameState state) {
        return getValue(state, -gameInfo.getMaxUtility(), gameInfo.getMaxUtility());
    }

    public double getUnboundedValueAndStoreStrategy(GameState state, DOCache doCache) {
        return getValue(state, -gameInfo.getMaxUtility(), gameInfo.getMaxUtility(), doCache);
    }

    public double getValue(GameState state, double alpha, double beta) {
        return getValue(state, alpha, beta, new NullDOCache());
    }

    public void printCache(){
        System.out.println("Cache size: "+cache.size());
    }

    private double getValue(GameState state, double alpha, double beta, DOCache doCache) {
        Double value = cache.get(state);
        p1Action = null;
        p2Action = null;
        tempAction = null;

//        if (cache.size() > 0)
//            System.out.println("Cache not empty!");

        if (value != null) {
            System.out.println("Cached : " + state.getHistory().toString());
            return value;
        }

//        if (SimAlphaBeta.COMPUTED) {
//            System.out.println(cache.size());
//            return -10000;
//        }

        boolean prune = false;

        if (state.isGameEnd()) {
            if (state instanceof SimultaneousGameState)
                SimAlphaBeta.FULLY_COMPUTED &= ((SimultaneousGameState) state).isActualGameEnd();
            return state.getUtilities()[player.getId()];
        }
        if (state.isPlayerToMoveNature()) {
            return getUtilityForNature(state, alpha, beta, doCache);
        } else {
            Stats.getInstance().increaseABStatesFor(player);
            for (Action minAction : getMinimizingActions(state)) {
                if (Killer.kill)
                    return Double.NaN;
                double tempAlpha = getTempAlpha(state, minAction, alpha, beta);

                if (Killer.kill)
                    return Double.NaN;
                if (tempAlpha <= beta && p1Action == null) {
                    storeAction(minAction);
                    storeAction(tempAction);
                }
                if (tempAlpha < beta) {
                    beta = tempAlpha;
                    storeAction(minAction);
                    storeAction(tempAction);
                } else {
                    prune = true;
                }
                tempAction = null;
                if (beta <= alpha) {
                    prune = true;
                    break;
                }
            }
            if (Killer.kill)
                return Double.NaN;
            assert p1Action != null;
            assert p2Action != null;
            storeToDOCache(state, doCache, beta);
            if (!prune)
                cache.put(state, beta);
            return beta;
        }
    }

    private void storeToDOCache(GameState state, DOCache doCache, double value) {
        ActionPureStrategy p1Strategy = getStrategyFor(state, state.getAllPlayers()[0]);
        ActionPureStrategy p2Strategy = getStrategyFor(state, state.getAllPlayers()[1]);
        ActionPureStrategy natureStrategy = null;

        if (state.getAllPlayers().length == 3)
            natureStrategy = getStrategyFor(state, state.getAllPlayers()[2]);

        Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> triplet = new Triplet<>(p1Strategy, p2Strategy, natureStrategy);

        doCache.setTempStrategy(triplet, gameInfo.getOpponent(player), new Result(-value, getStrategy()));
    }

    public MixedStrategy<ActionPureStrategy> getStrategy() {
        ActionPureStrategy pure = new ActionPureStrategy(player.getId() == 0 ? p2Action : p1Action);
        MixedStrategy<ActionPureStrategy> mixed = new MixedStrategy<>();

        mixed.put(pure, 1d);
        return mixed;
    }

    private ActionPureStrategy getStrategyFor(GameState state, Player player) {
        Sequence sequence = state.getSequenceFor(player);

        if (sequence.size() == 0)
            return null;
        return new ActionPureStrategy(sequence.getLast());
    }

    private double getInsideValue(GameState state, double alpha, double beta) {
        Double value = cache.get(state);

//        if (cache.size() > 0)
//            System.out.println("Cache not empty!" + cache.size());

        if (value != null) {
            System.out.println("IS CACHED !!");
            return value;
        }
//        System.out.println("IS not CACHED");

        boolean prune = false;

        if (state.isGameEnd()) {
            if (state instanceof SimultaneousGameState)
                SimAlphaBeta.FULLY_COMPUTED &= ((SimultaneousGameState) state).isActualGameEnd();
            return state.getUtilities()[player.getId()];
        }
        if (state.isPlayerToMoveNature()) {
            return getInsideUtilityForNature(state, alpha, beta);
        } else {
            Stats.getInstance().increaseABStatesFor(player);
            for (Action minAction : getMinimizingActions(state)) {
                if (Killer.kill)
                    return Double.NaN;
                double tempAlpha = getInsideTempAlpha(state, minAction, alpha, beta);

                if (Killer.kill)
                    return Double.NaN;
                if (beta <= tempAlpha)
                    prune = true;
                beta = Math.min(beta, tempAlpha);
                if (beta <= alpha) {
                    prune = true;
                    break;
                }
            }
            if (!prune)
                cache.put(state, beta);
            return beta;
        }
    }

    private double getTempAlpha(GameState state, Action minAction, double alpha, double beta) {
        double tempAlpha = alpha;

        for (Action maxAction : getMaximizingActions(state)) {
            if (Killer.kill)
                return Double.NaN;
            double value = getInsideValue(performActions(state, minAction, maxAction), tempAlpha, beta);

            if (Killer.kill)
                return Double.NaN;
            if (value >= tempAlpha && tempAction == null)
                tempAction = maxAction;
            if (value > tempAlpha) {
                tempAlpha = value;
                tempAction = maxAction;
            }
            if (beta <= tempAlpha) {
                assert tempAction != null;
                return tempAlpha;
            }
        }
        assert tempAction != null;
        return tempAlpha;
    }

    protected void storeAction(Action action) {
        if (action.getInformationSet().getPlayer().getId() == 0)
            p1Action = action;
        else
            p2Action = action;
    }

    private double getInsideTempAlpha(GameState state, Action minAction, double alpha, double beta) {
        double tempAlpha = alpha;

        for (Action maxAction : getMaximizingActions(state)) {
            if (Killer.kill)
                return Double.NaN;
            tempAlpha = Math.max(tempAlpha, getInsideValue(performActions(state, minAction, maxAction), tempAlpha, beta));
            if (Killer.kill)
                return Double.NaN;
            if (beta <= tempAlpha)
                return tempAlpha;
        }
        return tempAlpha;
    }

    public double getUtilityForNature(GameState state, double alpha, double beta, DOCache doCache) {
        double utility = 0;
        p1Action = null;
        p2Action = null;
        tempAction = null;
        List<Action> actions = expander.getActions(state);
        ListIterator<Action> iterator = actions.listIterator();

        while (iterator.hasNext()) {
            if (Killer.kill)
                return Double.NaN;
            Action action = iterator.next();
            double lowerBound = Math.max(-gameInfo.getMaxUtility(), getLowerBound(actions, state, alpha, state.getProbabilityOfNatureFor(action), utility, iterator.previousIndex()));
            double upperBound = Math.min(gameInfo.getMaxUtility(), getUpperBound(actions, state, beta, state.getProbabilityOfNatureFor(action), utility, iterator.previousIndex()));

            utility += state.getProbabilityOfNatureFor(action) * getValue(state.performAction(action), lowerBound, upperBound, doCache);
            if (Killer.kill)
                return Double.NaN;
        }
        return utility;
    }

    public double getInsideUtilityForNature(GameState state, double alpha, double beta) {
        double utility = 0;
        List<Action> actions = expander.getActions(state);
        ListIterator<Action> iterator = actions.listIterator();

        while (iterator.hasNext()) {
            Action action = iterator.next();
            double lowerBound = Math.max(-gameInfo.getMaxUtility(), getLowerBound(actions, state, alpha, state.getProbabilityOfNatureFor(action), utility, iterator.previousIndex()));
            double upperBound = Math.min(gameInfo.getMaxUtility(), getUpperBound(actions, state, beta, state.getProbabilityOfNatureFor(action), utility, iterator.previousIndex()));
            if (Killer.kill)
                return Double.NaN;
            utility += state.getProbabilityOfNatureFor(action) * getInsideValue(state.performAction(action), lowerBound, upperBound);
            if (Killer.kill)
                return Double.NaN;
        }
        return utility;
    }

    private double getUpperBound(List<Action> actions, GameState state, double upperBound, double probability, double utilityValue, int index) {
        ListIterator<Action> iterator = actions.listIterator();
        double utility = utilityValue;

        while (iterator.hasNext()) {
            Action action = iterator.next();

            if (iterator.previousIndex() > index)
                utility += state.getProbabilityOfNatureFor(action) * -gameInfo.getMaxUtility();
        }
        return (upperBound - utility) / probability + 1e-8;
    }

    private double getLowerBound(List<Action> actions, GameState state, double lowerBound, double probability, double utilityValue, int index) {
        ListIterator<Action> iterator = actions.listIterator();
        double utility = utilityValue;

        while (iterator.hasNext()) {
            Action action = iterator.next();

            if (iterator.previousIndex() > index)
                utility += state.getProbabilityOfNatureFor(action) * gameInfo.getMaxUtility();
        }
        return (lowerBound - utility) / probability - 1e-8;
    }

    public Action getTopLevelAction(Player player) {
        if (player.getId() == 0)
            return p1Action;
        return p2Action;
    }

    public int getCacheSize() {
        return cache.size();
    }

    protected abstract GameState performActions(GameState state, Action minAction, Action maxAction);

    protected abstract List<Action> getMaximizingActions(GameState state);

    protected abstract List<Action> getMinimizingActions(GameState state);
}
