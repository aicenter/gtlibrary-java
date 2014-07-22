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


package cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.doubleoracle.NFGDoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;

public abstract class DoubleOracle extends NFGDoubleOracle {

    public DoubleOracle(GameState rootState, Data data) {
        super(rootState, data.expander, data.gameInfo, data.config);
        Stats.getInstance().incrementStatesVisited();
//		Stats.getInstance().addState(rootState);
    }

    public MixedStrategy<ActionPureStrategy> getStrategyFor(Player player) {
        if (player.getId() == 0)
            return coreSolver.getPlayerOneStrategy();
        return coreSolver.getPlayerTwoStrategy();
    }

    public abstract double getGameValue();

    public abstract void generate();

    public abstract DOCache getCache();

}
