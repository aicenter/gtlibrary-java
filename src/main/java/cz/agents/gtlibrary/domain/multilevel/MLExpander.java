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


package cz.agents.gtlibrary.domain.multilevel;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.ArrayList;
import java.util.List;

public class MLExpander<I extends InformationSet> extends ExpanderImpl<I> {
    private static final long serialVersionUID = -2513286051108758L;

    public MLExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        MLGameState s = (MLGameState) gameState;

        List<Action> actions = new ArrayList<>();

        if (!gameState.isGameEnd()) {
            actions.add(new MLAction(1, s.getPlayerToMove(), getAlgorithmConfig().getInformationSetFor(s)));
            actions.add(new MLAction(2, s.getPlayerToMove(), getAlgorithmConfig().getInformationSetFor(s)));
        }

        return actions;
    }
}
