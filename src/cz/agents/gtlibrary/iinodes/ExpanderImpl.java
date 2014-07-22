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


package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.*;

import java.util.List;

public abstract class ExpanderImpl<I extends InformationSet> implements Expander<I> {

    private static final long serialVersionUID = -2367393002316400229L;

    private AlgorithmConfig<I> algConfig;

    public ExpanderImpl(AlgorithmConfig<I> algConfig) {
        this.algConfig = algConfig;
    }

    @Override
    public AlgorithmConfig<I> getAlgorithmConfig() {
        return algConfig;
    }

    @Override
    public List<Action> getActionsForUnknownIS(GameState gameState) {
        return getActions(algConfig.createInformationSetFor(gameState));
    }

    public void setAlgConfig(AlgorithmConfig<I> algConfig) {
        this.algConfig = algConfig;
    }

    @Override
    public List<Action> getActions(I informationSet) {
        GameState state = informationSet.getAllStates().iterator().next();
        List<Action> actions = getActions(state);

        if (!state.isPlayerToMoveNature())
            for (Action a : actions) {
                a.setInformationSet(informationSet);
            }
        return actions;
    }
}
