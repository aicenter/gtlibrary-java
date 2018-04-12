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


package cz.agents.gtlibrary.nfg.simalphabeta.cache;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Result;
import cz.agents.gtlibrary.utils.Triplet;

import java.util.HashMap;
import java.util.Map;

public class DOCacheRoot extends DOCacheImpl {

    private Map<Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy>, Result[]> strategies;
    private Map<Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy>, Result> p1TempStrategies;
    private Map<Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy>, Result> p2TempStrategies;

    public DOCacheRoot() {
        super();
        this.strategies = new HashMap<>();
        this.p1TempStrategies = new HashMap<>();
        this.p2TempStrategies = new HashMap<>();
    }

    @Override
    public void setStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, ActionPureStrategy strategy3, Result[] results) {
        setStrategy(new Triplet<>(strategy1, strategy2, strategy3), results);
    }

    @Override
    public void setStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> strategyTriplet, Result[] results) {
        assert results != null;
        strategies.put(strategyTriplet, results);
    }

    @Override
    public void setStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, ActionPureStrategy strategy3, Result p1Result, Result p2Result) {
        setStrategy(new Triplet<>(strategy1, strategy2, strategy3), new Result[]{p1Result, p2Result});
    }

    @Override
    public void setStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> strategyTriplet, Result p1Result, Result p2Result) {
        setStrategy(strategyTriplet, new Result[]{p1Result, p2Result});
    }

    @Override
    public void setTempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet, Player player, Result result) {
        if (player.getId() == 0)
            p1TempStrategies.put(actionTriplet, result);
        else
            p2TempStrategies.put(actionTriplet, result);
    }

    @Override
    public Result getP1TempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet) {
        return p1TempStrategies.get(actionTriplet);
    }

    @Override
    public Result getP2TempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet) {
        return p2TempStrategies.get(actionTriplet);
    }

    @Override
    public Result[] getStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, ActionPureStrategy strategy3) {
        return getStrategy(new Triplet<>(strategy1, strategy2, strategy3));
    }

    @Override
    public Result[] getTempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet) {
        Result p1Strategy = getP1TempStrategy(actionTriplet);
        Result p2Strategy = getP2TempStrategy(actionTriplet);

        assert (p1Strategy != null && p2Strategy != null) || (p1Strategy == null && p2Strategy == null);
        return new Result[]{p1Strategy, p2Strategy};
    }

    @Override
    public Result[] getStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> strategyTriplet) {
        return strategies.get(strategyTriplet);
    }

    @Override
    public Map<Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy>, Result[]> getStrategies() {
        return strategies;
    }

}
